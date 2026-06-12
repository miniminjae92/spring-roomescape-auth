package roomescape.service.dto.reservation;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.service.dto.reservationtime.ReservationTimeResult;
import roomescape.service.dto.theme.ThemeResult;

public record ReservationResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme,
        ReservationStatus status
) {

    public static ReservationResult from(Reservation reservation) {
        return from(reservation, ReservationTime.from(null, reservation.getStartAt()));
    }

    public static ReservationResult from(Reservation reservation, ReservationTime time) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResult.from(time),
                ThemeResult.from(reservation.getTheme()),
                reservation.getStatus()
        );
    }
}
