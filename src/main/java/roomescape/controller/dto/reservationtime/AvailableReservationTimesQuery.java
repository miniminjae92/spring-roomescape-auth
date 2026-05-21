package roomescape.controller.dto.reservationtime;

import java.time.LocalDate;
import roomescape.service.dto.reservationtime.AvailableReservationTimesCondition;

public record AvailableReservationTimesQuery(
        Long storeId,
        Long themeId,
        LocalDate date,
        Boolean available
) {

    public static AvailableReservationTimesQuery toQuery(Long storeId, Long themeId, LocalDate date, Boolean available) {
        return new AvailableReservationTimesQuery(storeId, themeId, date, available);
    }

    public AvailableReservationTimesCondition toCondition() {
        return new AvailableReservationTimesCondition(storeId, themeId, date, available);
    }
}
