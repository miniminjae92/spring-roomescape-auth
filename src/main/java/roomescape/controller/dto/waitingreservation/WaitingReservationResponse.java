package roomescape.controller.dto.waitingreservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.controller.dto.reservationtime.ReservationTimeResponse;
import roomescape.controller.dto.theme.ThemeResponse;
import roomescape.domain.WaitingReservationStatus;
import roomescape.service.dto.waitingreservation.WaitingReservationResult;

public record WaitingReservationResponse(
        Long id,
        Long storeId,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        WaitingReservationStatus status,
        LocalDateTime createdAt,
        Long promotedReservationId,
        Integer rank
) {

    public static WaitingReservationResponse from(WaitingReservationResult result) {
        return new WaitingReservationResponse(
                result.id(),
                result.storeId(),
                result.name(),
                result.date(),
                ReservationTimeResponse.from(result.time()),
                ThemeResponse.from(result.theme()),
                result.status(),
                result.createdAt(),
                result.promotedReservationId(),
                result.rank()
        );
    }
}
