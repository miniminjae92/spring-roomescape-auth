package roomescape.service.dto.waitingreservation;

import java.time.LocalDate;

public record CreateWaitingReservationCommand(
        Long storeId,
        Long memberId,
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
