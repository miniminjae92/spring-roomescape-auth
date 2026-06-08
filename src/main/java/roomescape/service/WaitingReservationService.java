package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.WaitingReservation;
import roomescape.domain.WaitingReservationStatus;
import roomescape.global.exception.waitingreservation.DuplicateWaitingReservationException;
import roomescape.global.exception.waitingreservation.WaitingReservationAccessDeniedException;
import roomescape.global.exception.waitingreservation.WaitingReservationNotAllowedException;
import roomescape.global.exception.waitingreservation.WaitingReservationNotFoundException;
import roomescape.global.exception.waitingreservation.WaitingReservationStateException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.StoreManagerRepository;
import roomescape.repository.WaitingReservationRepository;
import roomescape.service.dto.reservation.ReservationPagingCondition;
import roomescape.service.dto.waitingreservation.CreateWaitingReservationCommand;
import roomescape.service.dto.waitingreservation.WaitingReservationResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WaitingReservationService {

    private final WaitingReservationRepository waitingReservationRepository;
    private final ReservationRepository reservationRepository;
    private final StoreManagerRepository storeManagerRepository;
    private final ReservationSlotResolver slotResolver;
    private final Clock clock;

    @Transactional
    public WaitingReservationResult create(CreateWaitingReservationCommand command) {
        ReservationSlot slot = slotResolver.resolve(
                command.storeId(),
                command.date(),
                command.timeId(),
                command.themeId()
        );
        if (slot.isClosedForUser(clock)) {
            throw new WaitingReservationNotAllowedException("예약 시작 10분 전부터는 대기를 신청할 수 없습니다.");
        }

        Reservation reservation = reservationRepository.findActiveBySlotForUpdate(slot)
                .orElseThrow(() -> new WaitingReservationNotAllowedException(
                        "예약 가능한 시간에는 대기를 신청할 수 없습니다."
                ));
        if (reservation.isOwnedBy(command.memberId())) {
            throw new WaitingReservationNotAllowedException("본인의 예약에는 대기를 신청할 수 없습니다.");
        }
        if (waitingReservationRepository.existsActiveByMemberAndSlot(command.memberId(), slot)) {
            throw new DuplicateWaitingReservationException("이미 신청한 예약 대기입니다.");
        }

        WaitingReservation waiting = WaitingReservation.createNew(
                command.memberId(),
                command.name(),
                slot,
                LocalDateTime.now(clock)
        );
        try {
            return WaitingReservationResult.from(waitingReservationRepository.save(waiting));
        } catch (DuplicateKeyException e) {
            throw new DuplicateWaitingReservationException("이미 신청한 예약 대기입니다.");
        }
    }

    public List<WaitingReservationResult> getActiveByMember(
            Long memberId,
            ReservationPagingCondition condition
    ) {
        return waitingReservationRepository.findActiveByMemberId(
                        memberId,
                        condition.size(),
                        condition.offset()
                ).stream()
                .map(row -> WaitingReservationResult.from(row.waitingReservation(), row.rank()))
                .toList();
    }

    public List<WaitingReservationResult> getManaged(
            Long managerId,
            ReservationPagingCondition condition
    ) {
        List<Long> storeIds = storeManagerRepository.findStoreIdsByMemberId(managerId);
        return waitingReservationRepository.findAllByStoreIds(
                        storeIds,
                        condition.size(),
                        condition.offset()
                ).stream()
                .map(WaitingReservationResult::from)
                .toList();
    }

    @Transactional
    public WaitingReservationResult cancel(Long waitingId, Long memberId) {
        WaitingReservation waiting = getForUpdate(waitingId);
        if (!waiting.isOwnedBy(memberId)) {
            throw new WaitingReservationAccessDeniedException("접근 권한이 없습니다.");
        }
        if (waiting.getSlot().isClosedForUser(clock)) {
            throw new WaitingReservationNotAllowedException("예약 시작 10분 전부터는 대기를 취소할 수 없습니다.");
        }
        return cancelWaiting(waiting);
    }

    @Transactional
    public WaitingReservationResult cancelByAdmin(Long waitingId, Long managerId) {
        WaitingReservation waiting = getForUpdate(waitingId);
        validateManageable(waiting, managerId);
        return cancelWaiting(waiting);
    }

    @Transactional
    public void deleteByAdmin(Long waitingId, Long managerId) {
        WaitingReservation waiting = getForUpdate(waitingId);
        validateManageable(waiting, managerId);
        waitingReservationRepository.deleteById(waitingId);
    }

    private WaitingReservationResult cancelWaiting(WaitingReservation waiting) {
        WaitingReservation cancelled = waiting.cancel();
        int updated = waitingReservationRepository.updateStatus(
                cancelled,
                WaitingReservationStatus.WAITING
        );
        if (updated != 1) {
            throw new WaitingReservationStateException("이미 처리된 예약 대기입니다.");
        }
        return WaitingReservationResult.from(cancelled);
    }

    private WaitingReservation getForUpdate(Long waitingId) {
        return waitingReservationRepository.findByIdForUpdate(waitingId)
                .orElseThrow(() -> new WaitingReservationNotFoundException("해당 예약 대기를 찾을 수 없습니다."));
    }

    private void validateManageable(WaitingReservation waiting, Long managerId) {
        if (!storeManagerRepository.existsByStoreIdAndMemberId(waiting.getStoreId(), managerId)) {
            throw new WaitingReservationAccessDeniedException("접근 권한이 없습니다.");
        }
    }
}
