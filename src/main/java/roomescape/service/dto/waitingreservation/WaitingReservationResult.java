package roomescape.service.dto.waitingreservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.WaitingReservation;
import roomescape.domain.WaitingReservationStatus;
import roomescape.service.dto.reservationtime.ReservationTimeResult;
import roomescape.service.dto.theme.ThemeResult;

public record WaitingReservationResult(
        Long id,
        Long storeId,
        String name,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme,
        WaitingReservationStatus status,
        LocalDateTime createdAt,
        Long promotedReservationId,
        Integer rank
) {

    public static WaitingReservationResult from(WaitingReservation waiting) {
        return from(waiting, null);
    }

    public static WaitingReservationResult from(WaitingReservation waiting, Integer rank) {
        return new WaitingReservationResult(
                waiting.getId(),
                waiting.getStoreId(),
                waiting.getName(),
                waiting.getDate(),
                ReservationTimeResult.from(waiting.getTime()),
                ThemeResult.from(waiting.getTheme()),
                waiting.getStatus(),
                waiting.getCreatedAt(),
                waiting.getPromotedReservationId(),
                rank
        );
    }
}
