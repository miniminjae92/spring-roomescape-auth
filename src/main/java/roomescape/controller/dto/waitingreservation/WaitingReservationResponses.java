package roomescape.controller.dto.waitingreservation;

import java.util.List;

public record WaitingReservationResponses(
        List<WaitingReservationResponse> waitingReservations
) {
}
