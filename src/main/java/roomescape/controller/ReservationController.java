package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.reservation.ReservationRequest;
import roomescape.controller.dto.reservation.ReservationResponse;
import roomescape.controller.dto.reservation.ReservationResponses;
import roomescape.controller.dto.reservation.ReservationScheduleRequest;
import roomescape.global.auth.LoginMember;
import roomescape.global.auth.LoginRequired;
import roomescape.global.auth.SessionManager;
import roomescape.service.ReservationService;
import roomescape.service.dto.reservation.CancelReservationCommand;
import roomescape.service.dto.reservation.ReservationPagingCondition;
import roomescape.service.dto.reservation.ReservationResult;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    @LoginRequired
    public ResponseEntity<ReservationResponses> getReservations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ReservationPagingCondition condition = new ReservationPagingCondition(page, size);
        LoginMember loginMember = getLoginMember(request);
        List<ReservationResponse> responses = reservationService.getReservationHistoryByMember(loginMember.id(), condition).stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(new ReservationResponses(responses));
    }

    @PostMapping
    @LoginRequired
    public ResponseEntity<ReservationResponse> createReservation(
            HttpServletRequest httpRequest,
            @Valid @RequestBody ReservationRequest request
    ) {
        ReservationResult reservationResult = reservationService.createReservation(request.toCommand(getLoginMember(httpRequest)));
        ReservationResponse response = ReservationResponse.from(reservationResult);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}/schedule")
    @LoginRequired
    public ResponseEntity<ReservationResponse> changeReservationSchedule(
            HttpServletRequest httpRequest,
            @PathVariable Long id,
            @Valid @RequestBody ReservationScheduleRequest request
    ) {
        ReservationResult result = reservationService.changeReservationSchedule(request.toCommand(id, getLoginMember(httpRequest)));
        return ResponseEntity.ok(ReservationResponse.from(result));
    }

    @PostMapping("/{id}/cancellations")
    @LoginRequired
    public ResponseEntity<ReservationResponse> cancelReservation(
            HttpServletRequest httpRequest,
            @PathVariable Long id
    ) {
        ReservationResult result = reservationService.cancelReservation(
                new CancelReservationCommand(id, getLoginMember(httpRequest).id())
        );
        return ResponseEntity.ok(ReservationResponse.from(result));
    }

    private LoginMember getLoginMember(HttpServletRequest request) {
        return (LoginMember) request.getAttribute(SessionManager.LOGIN_MEMBER_ATTRIBUTE);
    }
}
