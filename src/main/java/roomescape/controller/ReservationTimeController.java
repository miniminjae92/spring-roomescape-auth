package roomescape.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.reservationtime.AvailableReservationTimesQuery;
import roomescape.controller.dto.reservationtime.AvailableReservationTimesResponse;
import roomescape.controller.dto.reservationtime.ReservationTimeResponse;
import roomescape.controller.dto.reservationtime.ReservationTimeResponses;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.reservationtime.AvailableReservationTimesResult;

@RestController
@RequestMapping("/reservation-times")
@RequiredArgsConstructor
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @GetMapping
    public ResponseEntity<ReservationTimeResponses> getReservationTimes() {
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.getReservationTimes().stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(new ReservationTimeResponses(reservationTimes));
    }

    @GetMapping("/available")
    public ResponseEntity<AvailableReservationTimesResponse> getAvailableReservationTimes(
            @RequestParam Long storeId,
            @RequestParam Long themeId,
            @RequestParam LocalDate date,
            @RequestParam(required = false) Boolean available
    ) {
        AvailableReservationTimesResult result = reservationTimeService.getAvailableReservationTimes(
                AvailableReservationTimesQuery.toQuery(storeId, themeId, date, available).toCondition());
        return ResponseEntity.ok(AvailableReservationTimesResponse.from(result));
    }
}
