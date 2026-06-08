package roomescape.domain;

import java.time.LocalDate;
import lombok.Getter;
import roomescape.global.exception.reservation.CancelledReservationException;
import roomescape.global.exception.reservation.InvalidReservationException;
import roomescape.global.exception.reservation.SameReservationScheduleException;

@Getter
public class Reservation {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 20;
    private static final String NAME_PATTERN = "^[가-힣a-zA-Z ]+$";

    private final Long id;
    private final Long memberId;
    private final String name;
    private final ReservationSlot slot;
    private final ReservationStatus status;

    private Reservation(Long id, Long storeId, Long memberId, String name, LocalDate date, ReservationTime time, Theme theme,
                        ReservationStatus status) {
        validateMemberId(memberId);
        validateName(name);
        this.id = id;
        this.memberId = memberId;
        this.name = name;
        this.slot = ReservationSlot.of(storeId, date, time, theme);
        this.status = status;
    }

    public static Reservation createNew(Long storeId, Long memberId, String name, LocalDate date, ReservationTime time,
                                        Theme theme) {
        return new Reservation(null, storeId, memberId, name, date, time, theme, ReservationStatus.RESERVED);
    }

    public static Reservation from(Long id, Long storeId, Long memberId, String name, LocalDate date, ReservationTime time,
                                   Theme theme, ReservationStatus status) {
        return new Reservation(id, storeId, memberId, name, date, time, theme, status);
    }

    public Reservation changeSchedule(LocalDate date, ReservationTime time) {
        validateReserved();
        validateDifferentSchedule(date, time);
        return new Reservation(id, getStoreId(), memberId, name, date, time, getTheme(), status);
    }

    public Reservation cancel() {
        validateReserved();
        return new Reservation(id, getStoreId(), memberId, name, getDate(), getTime(), getTheme(),
                ReservationStatus.CANCELLED);
    }

    public boolean hasSameSchedule(LocalDate date, ReservationTime time) {
        return slot.isSameSchedule(date, time);
    }

    public boolean isOwnedBy(Long memberId) {
        return this.memberId != null && this.memberId.equals(memberId);
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

    private void validateDifferentSchedule(LocalDate date, ReservationTime time) {
        if (hasSameSchedule(date, time)) {
            throw new SameReservationScheduleException("이미 같은 일정으로 예약되어 있습니다.");
        }
    }

    private void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new InvalidReservationException("예약 회원은 필수입니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidReservationException("이름은 비어있을 수 없습니다.");
        }
        if (name.length() < MIN_NAME_LENGTH) {
            throw new InvalidReservationException("이름은 " + MIN_NAME_LENGTH + "자 이상이어야 합니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidReservationException("이름은 " + MAX_NAME_LENGTH + "자 이하여야 합니다.");
        }
        if (!name.matches(NAME_PATTERN)) {
            throw new InvalidReservationException("이름은 완성형 한글, 영문, 공백만 허용합니다.");
        }
    }

    private void validateReserved() {
        if (status == ReservationStatus.CANCELLED) {
            throw new CancelledReservationException("이미 취소된 예약입니다.");
        }
    }
}
