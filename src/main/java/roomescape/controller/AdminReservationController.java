package roomescape.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.reservation.ReservationResponse;
import roomescape.controller.dto.reservation.ReservationResponses;
import roomescape.global.auth.Authenticated;
import roomescape.global.auth.LoginMember;
import roomescape.global.auth.LoginRequired;
import roomescape.service.ReservationService;
import roomescape.service.dto.reservation.ReservationPagingCondition;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
@LoginRequired(managerOnly = true)
public class AdminReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<ReservationResponses> getReservations(
            @Authenticated LoginMember loginMember,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ReservationPagingCondition condition = new ReservationPagingCondition(page, size);
        List<ReservationResponse> responses = reservationService.getManagedReservations(loginMember.id(), condition).stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(new ReservationResponses(responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@Authenticated LoginMember loginMember, @PathVariable Long id) {
        reservationService.deleteReservation(id, loginMember.id());
        return ResponseEntity.noContent().build();
    }
}
