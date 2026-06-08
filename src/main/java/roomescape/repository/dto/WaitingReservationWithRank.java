package roomescape.repository.dto;

import roomescape.domain.WaitingReservation;

public record WaitingReservationWithRank(
        WaitingReservation waitingReservation,
        int rank
) {
}
