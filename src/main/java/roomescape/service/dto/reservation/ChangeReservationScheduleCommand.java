package roomescape.service.dto.reservation;

import java.time.LocalDate;

public record ChangeReservationScheduleCommand(
        Long reservationId,
        Long memberId,
        LocalDate date,
        Long timeId
) {
}
