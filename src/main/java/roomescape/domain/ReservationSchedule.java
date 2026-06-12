package roomescape.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import roomescape.global.exception.reservation.InvalidReservationException;

@Getter
public class ReservationSchedule {

    private final LocalDateTime reservationDateTime;

    private ReservationSchedule(LocalDateTime reservationDateTime) {
        validateNotNull(reservationDateTime);
        this.reservationDateTime = reservationDateTime;
    }

    public static ReservationSchedule of(LocalDateTime reservationDateTime) {
        return new ReservationSchedule(reservationDateTime);
    }

    public static ReservationSchedule create(LocalDateTime reservationDateTime, LocalDateTime currentDateTime) {
        ReservationSchedule schedule = new ReservationSchedule(reservationDateTime);
        schedule.validateReservable(currentDateTime);
        return schedule;
    }

    public boolean hasSameSchedule(ReservationSchedule other) {
        return this.reservationDateTime.equals(other.reservationDateTime);
    }

    public boolean isExpired(LocalDateTime currentDateTime) {
        return reservationDateTime.isBefore(currentDateTime);
    }

    private void validateReservable(LocalDateTime currentDateTime) {
        validateNotNull(currentDateTime);
        if (isExpired(currentDateTime)) {
            throw new InvalidReservationException("과거 날짜/시간으로는 예약할 수 없습니다.");
        }
        if (reservationDateTime.isAfter(currentDateTime.plusDays(30))) {
            throw new InvalidReservationException("30일을 초과한 날짜로는 예약할 수 없습니다.");
        }
    }

    private void validateNotNull(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new InvalidReservationException("예약 날짜, 시간은 필수입니다.");
        }
    }
}
