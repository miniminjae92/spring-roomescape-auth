package roomescape.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.waitingreservation.WaitingReservationResponse;
import roomescape.controller.dto.waitingreservation.WaitingReservationResponses;
import roomescape.global.auth.Authenticated;
import roomescape.global.auth.LoginMember;
import roomescape.global.auth.LoginRequired;
import roomescape.service.WaitingReservationService;
import roomescape.service.dto.reservation.ReservationPagingCondition;

@RestController
@RequestMapping("/admin/waiting-reservations")
@RequiredArgsConstructor
@LoginRequired(managerOnly = true)
public class AdminWaitingReservationController {

    private final WaitingReservationService waitingReservationService;

    @GetMapping
    public ResponseEntity<WaitingReservationResponses> getManaged(
            @Authenticated LoginMember member,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ReservationPagingCondition condition = new ReservationPagingCondition(page, size);
        List<WaitingReservationResponse> responses = waitingReservationService
                .getManaged(member.id(), condition)
                .stream()
                .map(WaitingReservationResponse::from)
                .toList();
        return ResponseEntity.ok(new WaitingReservationResponses(responses));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<WaitingReservationResponse> cancel(
            @Authenticated LoginMember member,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                WaitingReservationResponse.from(waitingReservationService.cancelByAdmin(id, member.id()))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Authenticated LoginMember member,
            @PathVariable Long id
    ) {
        waitingReservationService.deleteByAdmin(id, member.id());
        return ResponseEntity.noContent().build();
    }
}
