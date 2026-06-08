package roomescape.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.waitingreservation.WaitingReservationRequest;
import roomescape.controller.dto.waitingreservation.WaitingReservationResponse;
import roomescape.controller.dto.waitingreservation.WaitingReservationResponses;
import roomescape.global.auth.Authenticated;
import roomescape.global.auth.LoginMember;
import roomescape.global.auth.LoginRequired;
import roomescape.service.WaitingReservationService;
import roomescape.service.dto.reservation.ReservationPagingCondition;

@RestController
@RequestMapping("/waiting-reservations")
@RequiredArgsConstructor
@LoginRequired
public class WaitingReservationController {

    private final WaitingReservationService waitingReservationService;

    @GetMapping
    public ResponseEntity<WaitingReservationResponses> getActive(
            @Authenticated LoginMember member,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ReservationPagingCondition condition = new ReservationPagingCondition(page, size);
        List<WaitingReservationResponse> responses = waitingReservationService
                .getActiveByMember(member.id(), condition)
                .stream()
                .map(WaitingReservationResponse::from)
                .toList();
        return ResponseEntity.ok(new WaitingReservationResponses(responses));
    }

    @PostMapping
    public ResponseEntity<WaitingReservationResponse> create(
            @Authenticated LoginMember member,
            @Valid @RequestBody WaitingReservationRequest request
    ) {
        WaitingReservationResponse response = WaitingReservationResponse.from(
                waitingReservationService.create(request.toCommand(member))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<WaitingReservationResponse> cancel(
            @Authenticated LoginMember member,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                WaitingReservationResponse.from(waitingReservationService.cancel(id, member.id()))
        );
    }
}
