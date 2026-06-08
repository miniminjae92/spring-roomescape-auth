package roomescape.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import roomescape.global.exception.reservation.InvalidReservationException;

@Getter
public class ReservationSlot {

    private static final int CLOSING_MINUTES = 10;

    private final Long storeId;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private ReservationSlot(Long storeId, LocalDate date, ReservationTime time, Theme theme) {
        validateNotNull(storeId, date, time, theme);
        this.storeId = storeId;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationSlot of(Long storeId, LocalDate date, ReservationTime time, Theme theme) {
        return new ReservationSlot(storeId, date, time, theme);
    }

    public ReservationSlot changeSchedule(LocalDate date, ReservationTime time) {
        return new ReservationSlot(storeId, date, time, theme);
    }

    public boolean isSameSchedule(LocalDate date, ReservationTime time) {
        return this.date.equals(date) && this.time.hasSameStartAt(time);
    }

    public boolean isSameSlot(ReservationSlot other) {
        return storeId.equals(other.storeId)
                && date.equals(other.date)
                && time.getId().equals(other.time.getId())
                && theme.getId().equals(other.theme.getId());
    }

    public boolean isClosedForUser(Clock clock) {
        return !LocalDateTime.now(clock).isBefore(startAt().minusMinutes(CLOSING_MINUTES));
    }

    public boolean hasStarted(Clock clock) {
        return !LocalDateTime.now(clock).isBefore(startAt());
    }

    public LocalDateTime startAt() {
        return LocalDateTime.of(date, time.getStartAt());
    }

    private void validateNotNull(Long storeId, LocalDate date, ReservationTime time, Theme theme) {
        if (Objects.isNull(storeId)) {
            throw new InvalidReservationException("예약 매장은 필수입니다.");
        }
        if (Objects.isNull(date) || Objects.isNull(time)) {
            throw new InvalidReservationException("예약 날짜, 시간은 필수입니다.");
        }
        if (Objects.isNull(theme)) {
            throw new InvalidReservationException("예약 테마는 필수입니다.");
        }
    }
}
