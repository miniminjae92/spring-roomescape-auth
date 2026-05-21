package roomescape.service.dto.reservation;

import java.time.LocalDate;

public record CreateReservationCommand(
        Long storeId,
        Long memberId,
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
