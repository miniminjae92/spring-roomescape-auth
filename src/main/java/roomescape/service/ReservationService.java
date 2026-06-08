package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.WaitingReservation;
import roomescape.domain.WaitingReservationStatus;
import roomescape.global.exception.reservation.CancelledReservationException;
import roomescape.global.exception.reservation.DuplicateReservationException;
import roomescape.global.exception.reservation.ExpiredReservationCancelException;
import roomescape.global.exception.reservation.ExpiredReservationChangeException;
import roomescape.global.exception.reservation.InvalidReservationException;
import roomescape.global.exception.reservation.ReservationAccessDeniedException;
import roomescape.global.exception.reservation.ReservationHasWaitingException;
import roomescape.global.exception.reservation.ReservationNotFoundException;
import roomescape.global.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.StoreManagerRepository;
import roomescape.repository.WaitingReservationRepository;
import roomescape.service.dto.reservation.CancelReservationCommand;
import roomescape.service.dto.reservation.ChangeReservationScheduleCommand;
import roomescape.service.dto.reservation.CreateReservationCommand;
import roomescape.service.dto.reservation.ReservationPagingCondition;
import roomescape.service.dto.reservation.ReservationResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final StoreManagerRepository storeManagerRepository;
    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationSlotResolver slotResolver;
    private final Clock clock;

    public List<ReservationResult> getReservations(ReservationPagingCondition condition) {
        return reservationRepository.findAll(condition.size(), condition.offset()).stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationResult> getManagedReservations(Long managerId, ReservationPagingCondition condition) {
        List<Long> storeIds = storeManagerRepository.findStoreIdsByMemberId(managerId);
        return reservationRepository.findAllByStoreIds(storeIds, condition.size(), condition.offset()).stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationResult> getReservationHistoryByMember(
            Long memberId,
            ReservationPagingCondition condition
    ) {
        return reservationRepository.findAllByMemberId(memberId, condition.size(), condition.offset()).stream()
                .map(ReservationResult::from)
                .toList();
    }

    @Transactional
    public ReservationResult createReservation(CreateReservationCommand command) {
        ReservationSlot slot = resolveSlot(command);
        validateUserReservable(slot);
        return saveReservation(command, slot);
    }

    @Transactional
    public ReservationResult createAdminReservation(CreateReservationCommand command, Long managerId) {
        ReservationSlot slot = resolveSlot(command);
        validateManageableStore(slot.getStoreId(), managerId);
        validateAdminReservable(slot);
        return saveReservation(command, slot);
    }

    @Transactional
    public ReservationResult changeReservationSchedule(ChangeReservationScheduleCommand command) {
        Reservation reservation = getOwnedReservationForUpdate(command.reservationId(), command.memberId());
        validateUserChangeable(reservation);

        ReservationTime time = reservationTimeRepository.findById(command.timeId())
                .orElseThrow(() -> new ReservationTimeNotFoundException("선택한 예약 시간이 존재하지 않습니다."));
        Reservation changed = reservation.changeSchedule(command.date(), time);
        validateUserReservable(changed.getSlot());
        validateAvailableSlot(changed.getSlot());

        try {
            if (reservationRepository.updateScheduleIfReserved(changed) != 1) {
                throw new CancelledReservationException("이미 처리된 예약입니다.");
            }
        } catch (DuplicateKeyException e) {
            throw new DuplicateReservationException("이미 예약된 시간입니다.");
        }
        promoteOldestWaiting(reservation.getSlot());
        return ReservationResult.from(changed);
    }

    @Transactional
    public ReservationResult cancelReservation(CancelReservationCommand command) {
        Reservation reservation = getOwnedReservationForUpdate(command.reservationId(), command.memberId());
        if (reservation.getSlot().hasStarted(clock)) {
            throw new ExpiredReservationCancelException("지난 예약은 취소할 수 없습니다.");
        }
        if (reservation.getSlot().isClosedForUser(clock)) {
            throw new ExpiredReservationCancelException("예약 시작 10분 전부터는 취소할 수 없습니다.");
        }
        Reservation cancelled = cancelLockedReservation(reservation);
        promoteOldestWaiting(reservation.getSlot());
        return ReservationResult.from(cancelled);
    }

    @Transactional
    public ReservationResult cancelReservationByAdmin(Long reservationId, Long managerId) {
        Reservation reservation = getReservationForUpdate(reservationId);
        validateManageableStore(reservation.getStoreId(), managerId);
        Reservation cancelled = cancelLockedReservation(reservation);
        if (!reservation.getSlot().hasStarted(clock)) {
            promoteOldestWaiting(reservation.getSlot());
        }
        return ReservationResult.from(cancelled);
    }

    @Transactional
    public void deleteReservation(Long reservationId, Long managerId) {
        Reservation reservation = getReservationForUpdate(reservationId);
        validateManageableStore(reservation.getStoreId(), managerId);
        if (waitingReservationRepository.existsActiveBySlot(reservation.getSlot())) {
            throw new ReservationHasWaitingException("활성 예약 대기가 있어 예약을 삭제할 수 없습니다.");
        }
        reservationRepository.deleteById(reservationId);
    }

    @Transactional
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    private ReservationResult saveReservation(CreateReservationCommand command, ReservationSlot slot) {
        validateAvailableSlot(slot);
        Reservation reservation = Reservation.createNew(
                slot.getStoreId(),
                command.memberId(),
                command.name(),
                slot.getDate(),
                slot.getTime(),
                slot.getTheme()
        );
        try {
            return ReservationResult.from(reservationRepository.save(reservation));
        } catch (DuplicateKeyException e) {
            throw new DuplicateReservationException("이미 예약된 시간입니다.");
        }
    }

    private Reservation cancelLockedReservation(Reservation reservation) {
        Reservation cancelled = reservation.cancel();
        if (reservationRepository.updateStatus(cancelled, ReservationStatus.RESERVED) != 1) {
            throw new CancelledReservationException("이미 처리된 예약입니다.");
        }
        return cancelled;
    }

    private void promoteOldestWaiting(ReservationSlot slot) {
        waitingReservationRepository.findOldestActiveBySlotForUpdate(slot)
                .ifPresent(waiting -> {
                    Reservation promoted = reservationRepository.save(
                            Reservation.createNew(
                                    waiting.getStoreId(),
                                    waiting.getMemberId(),
                                    waiting.getName(),
                                    waiting.getDate(),
                                    waiting.getTime(),
                                    waiting.getTheme()
                            )
                    );
                    WaitingReservation promotedWaiting = waiting.promote(promoted.getId());
                    int updated = waitingReservationRepository.updateStatus(
                            promotedWaiting,
                            WaitingReservationStatus.WAITING
                    );
                    if (updated != 1) {
                        throw new CancelledReservationException("예약 대기 승격 상태가 변경되었습니다.");
                    }
                });
    }

    private ReservationSlot resolveSlot(CreateReservationCommand command) {
        return slotResolver.resolve(command.storeId(), command.date(), command.timeId(), command.themeId());
    }

    private Reservation getOwnedReservationForUpdate(Long reservationId, Long memberId) {
        Reservation reservation = getReservationForUpdate(reservationId);
        if (!reservation.isOwnedBy(memberId)) {
            throw new ReservationAccessDeniedException("접근 권한이 없습니다.");
        }
        return reservation;
    }

    private Reservation getReservationForUpdate(Long reservationId) {
        return reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("해당 예약을 찾을 수 없습니다."));
    }

    private void validateUserReservable(ReservationSlot slot) {
        validateReservationRange(slot.getDate());
        if (slot.hasStarted(clock)) {
            throw new InvalidReservationException("과거 날짜/시간으로는 예약할 수 없습니다.");
        }
        if (slot.isClosedForUser(clock)) {
            throw new InvalidReservationException("예약 시작 10분 전부터는 예약할 수 없습니다.");
        }
    }

    private void validateAdminReservable(ReservationSlot slot) {
        validateReservationRange(slot.getDate());
        if (slot.hasStarted(clock)) {
            throw new InvalidReservationException("이미 시작한 예약은 생성할 수 없습니다.");
        }
    }

    private void validateReservationRange(LocalDate date) {
        LocalDate today = LocalDate.now(clock);
        if (date.isBefore(today)) {
            throw new InvalidReservationException("과거 날짜/시간으로는 예약할 수 없습니다.");
        }
        if (date.isAfter(today.plusDays(30))) {
            throw new InvalidReservationException("30일을 초과한 날짜로는 예약할 수 없습니다.");
        }
    }

    private void validateUserChangeable(Reservation reservation) {
        if (reservation.getSlot().hasStarted(clock)) {
            throw new ExpiredReservationChangeException("지난 예약은 변경할 수 없습니다.");
        }
        if (reservation.getSlot().isClosedForUser(clock)) {
            throw new ExpiredReservationChangeException("예약 시작 10분 전부터는 변경할 수 없습니다.");
        }
    }

    private void validateAvailableSlot(ReservationSlot slot) {
        if (reservationRepository.existsActiveBySlot(slot)) {
            throw new DuplicateReservationException("이미 예약된 시간입니다.");
        }
    }

    private void validateManageableStore(Long storeId, Long managerId) {
        if (!storeManagerRepository.existsByStoreIdAndMemberId(storeId, managerId)) {
            throw new ReservationAccessDeniedException("접근 권한이 없습니다.");
        }
    }
}
