package roomescape.service.dto.reservation;

public record CancelReservationCommand(
        Long reservationId,
        Long memberId
) {
}
