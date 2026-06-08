package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.global.exception.waitingreservation.InvalidWaitingReservationException;
import roomescape.global.exception.waitingreservation.WaitingReservationStateException;

@Getter
public class WaitingReservation {

    private final Long id;
    private final Long memberId;
    private final String name;
    private final ReservationSlot slot;
    private final WaitingReservationStatus status;
    private final LocalDateTime createdAt;
    private final Long promotedReservationId;

    private WaitingReservation(
            Long id,
            Long memberId,
            String name,
            ReservationSlot slot,
            WaitingReservationStatus status,
            LocalDateTime createdAt,
            Long promotedReservationId
    ) {
        validate(memberId, name, slot, status, createdAt);
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.slot = slot;
        this.status = status;
        this.createdAt = createdAt;
        this.promotedReservationId = promotedReservationId;
    }

    public static WaitingReservation createNew(
            Long memberId,
            String name,
            ReservationSlot slot,
            LocalDateTime createdAt
    ) {
        return new WaitingReservation(
                null, memberId, name, slot, WaitingReservationStatus.WAITING, createdAt, null
        );
    }

    public static WaitingReservation from(
            Long id,
            Long memberId,
            String name,
            ReservationSlot slot,
            WaitingReservationStatus status,
            LocalDateTime createdAt,
            Long promotedReservationId
    ) {
        return new WaitingReservation(id, memberId, name, slot, status, createdAt, promotedReservationId);
    }

    public WaitingReservation cancel() {
        validateWaiting();
        return new WaitingReservation(
                id, memberId, name, slot, WaitingReservationStatus.CANCELLED, createdAt, null
        );
    }

    public WaitingReservation promote(Long reservationId) {
        validateWaiting();
        if (reservationId == null) {
            throw new InvalidWaitingReservationException("승격된 예약 식별자는 필수입니다.");
        }
        return new WaitingReservation(
                id, memberId, name, slot, WaitingReservationStatus.PROMOTED, createdAt, reservationId
        );
    }

    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public Long getStoreId() {
        return slot.getStoreId();
    }

    public LocalDate getDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    private void validateWaiting() {
        if (status != WaitingReservationStatus.WAITING) {
            throw new WaitingReservationStateException("처리할 수 없는 예약 대기 상태입니다.");
        }
    }

    private void validate(
            Long memberId,
            String name,
            ReservationSlot slot,
            WaitingReservationStatus status,
            LocalDateTime createdAt
    ) {
        if (memberId == null || name == null || name.isBlank() || slot == null || status == null || createdAt == null) {
            throw new InvalidWaitingReservationException("예약 대기 정보는 비어있을 수 없습니다.");
        }
    }
}
