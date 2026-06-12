package roomescape.controller.dto.reservation;

import java.time.LocalDate;
import roomescape.controller.dto.reservationtime.ReservationTimeResponse;
import roomescape.controller.dto.theme.ThemeResponse;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.reservation.ReservationResult;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status
) {

    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
                result.id(),
                result.name(),
                result.date(),
                ReservationTimeResponse.from(result.time()),
                ThemeResponse.from(result.theme()),
                result.status()
        );
    }
}
