package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import roomescape.global.exception.reservation.InvalidReservationException;

@Getter
public class ReservationSchedule {

    private final LocalDate date;
    private final ReservationTime time;

    private ReservationSchedule(LocalDate date, ReservationTime time) {
        validateNotNull(date, time);
        this.date = date;
        this.time = time;
    }

    public static ReservationSchedule of(LocalDate date, ReservationTime time) {
        return new ReservationSchedule(date, time);
    }

    public boolean hasSameSchedule(LocalDate date, ReservationTime time) {
        return this.date.equals(date) && this.time.hasSameStartAt(time);
    }

    public boolean isExpired(LocalDate today, LocalTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(this.date, this.time.getStartAt());
        LocalDateTime currentDateTime = LocalDateTime.of(today, now);
        return reservationDateTime.isBefore(currentDateTime);
    }

    private void validateNotNull(LocalDate date, ReservationTime time) {
        if (date == null || time == null) {
            throw new InvalidReservationException("예약 날짜, 시간은 필수입니다.");
        }
    }
}
