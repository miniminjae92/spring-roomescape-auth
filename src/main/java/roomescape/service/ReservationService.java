package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservedTimes;
import roomescape.service.dto.reservation.CreateReservationCommand;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.global.exception.reservation.ExpiredReservationCancelException;
import roomescape.global.exception.reservation.ExpiredReservationChangeException;
import roomescape.global.exception.reservation.InvalidReservationException;
import roomescape.global.exception.reservation.ReservationAccessDeniedException;
import roomescape.global.exception.reservation.ReservationNotFoundException;
import roomescape.global.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.global.exception.store.StoreNotFoundException;
import roomescape.global.exception.theme.ThemeNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.StoreManagerRepository;
import roomescape.repository.StoreRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.reservation.ReservationPagingCondition;
import roomescape.service.dto.reservation.ReservationResult;
import roomescape.service.dto.reservation.ChangeReservationScheduleCommand;
import roomescape.service.dto.reservation.CancelReservationCommand;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final StoreManagerRepository storeManagerRepository;
    private final StoreRepository storeRepository;
    private final Clock clock;

    public List<ReservationResult> getManagedReservations(Long managerId, ReservationPagingCondition condition) {
        List<Long> storeIds = storeManagerRepository.findStoreIdsByMemberId(managerId);
        return reservationRepository.findAllByStoreIds(storeIds, condition.size(), condition.offset()).stream()
                .map(ReservationResult::from)
                .toList();
    }

    public List<ReservationResult> getReservationHistoryByMember(Long memberId, ReservationPagingCondition condition) {
        return reservationRepository.findAllByMemberId(memberId, condition.size(), condition.offset()).stream()
                .map(ReservationResult::from)
                .toList();
    }

    @Transactional
    public ReservationResult createReservation(CreateReservationCommand command) {
        ReservationTime time = getReservationTime(command);
        Theme theme = getTheme(command);
        validateStoreExists(command.storeId());
        validateReservableDateTime(command.date(), time);
        validateAvailableSlot(command.storeId(), theme.getId(), command.date(), time.getId());

        Reservation reservation = reservationRepository.save(
                Reservation.createNew(
                        command.storeId(),
                        command.memberId(),
                        command.name(),
                        command.date(),
                        time,
                        theme)
        );

        return ReservationResult.from(reservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void deleteReservation(Long reservationId, Long managerId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("해당 예약을 찾을 수 없습니다."));
        validateManageableReservation(reservation, managerId);
        reservationRepository.deleteById(reservationId);
    }

    @Transactional
    public ReservationResult changeReservationSchedule(ChangeReservationScheduleCommand command) {
        Reservation reservation = getReservation(command.reservationId(), command.memberId());
        validateChangeableReservation(reservation);
        ReservationTime time = getReservationTime(command.timeId());
        validateReservableDateTime(command.date(), time);

        validateAvailableSlot(reservation.getStoreId(), reservation.getTheme().getId(), command.date(), time.getId());

        Reservation changedReservation = reservation.changeSchedule(command.date(), time);
        return ReservationResult.from(reservationRepository.updateSchedule(changedReservation));
    }

    @Transactional
    public ReservationResult cancelReservation(CancelReservationCommand command) {
        Reservation reservation = getReservation(command.reservationId(), command.memberId());
        validateCancellableReservation(reservation);
        Reservation cancelledReservation = reservation.cancel();
        return ReservationResult.from(reservationRepository.updateStatus(cancelledReservation));
    }

    @NonNull
    private ReservationTime getReservationTime(CreateReservationCommand command) {
        return getReservationTime(command.timeId());
    }

    @NonNull
    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("선택한 예약 시간이 존재하지 않습니다."));
    }

    @NonNull
    private Theme getTheme(CreateReservationCommand command) {
        return themeRepository.findById(command.themeId())
                .orElseThrow(() -> new ThemeNotFoundException("선택한 테마가 존재하지 않습니다."));
    }

    @NonNull
    private Reservation getReservation(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("해당 예약을 찾을 수 없습니다."));
        if (!reservation.isOwnedBy(memberId)) {
            throw new ReservationAccessDeniedException("접근 권한이 없습니다.");
        }
        return reservation;
    }

    private void validateReservableDateTime(LocalDate date, ReservationTime time) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        LocalDateTime currentDateTime = LocalDateTime.of(today, now);

        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new InvalidReservationException("과거 날짜/시간으로는 예약할 수 없습니다.");
        }
        if (date.isAfter(today.plusDays(30))) {
            throw new InvalidReservationException("30일을 초과한 날짜로는 예약할 수 없습니다.");
        }
    }

    private void validateStoreExists(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new StoreNotFoundException("선택한 매장이 존재하지 않습니다.");
        }
    }

    private void validateAvailableSlot(Long storeId, Long themeId, LocalDate date, Long timeId) {
        ReservedTimes reservedTimes = new ReservedTimes(
                reservationTimeRepository.findReservedTimeIds(storeId, themeId, date)
        );
        reservedTimes.validateAvailable(timeId);
    }

    private void validateChangeableReservation(Reservation reservation) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        if (reservation.isExpired(today, now)) {
            throw new ExpiredReservationChangeException("지난 예약은 변경할 수 없습니다.");
        }
    }

    private void validateCancellableReservation(Reservation reservation) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        if (reservation.isExpired(today, now)) {
            throw new ExpiredReservationCancelException("지난 예약은 취소할 수 없습니다.");
        }
    }

    private void validateManageableReservation(Reservation reservation, Long managerId) {
        if (!storeManagerRepository.existsByStoreIdAndMemberId(reservation.getStoreId(), managerId)) {
            throw new ReservationAccessDeniedException("접근 권한이 없습니다.");
        }
    }
}
