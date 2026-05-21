package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.reservationtime.ReservationTimeRequest;
import roomescape.controller.dto.reservationtime.ReservationTimeResponse;
import roomescape.global.auth.LoginRequired;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.reservationtime.ReservationTimeResult;

@RestController
@RequestMapping("/admin/reservation-times")
@RequiredArgsConstructor
@LoginRequired(managerOnly = true)
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @Valid @RequestBody ReservationTimeRequest request) {
        ReservationTimeResult reservationTimeResult = reservationTimeService.createReservationTime(request.toCommand());
        ReservationTimeResponse reservationTime = ReservationTimeResponse.from(reservationTimeResult);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationTime);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent().build();
    }
}
