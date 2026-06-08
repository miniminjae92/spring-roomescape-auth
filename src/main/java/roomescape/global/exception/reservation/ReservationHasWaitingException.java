package roomescape.global.exception.reservation;

import roomescape.global.exception.status.ConflictException;

public class ReservationHasWaitingException extends ConflictException {

    public ReservationHasWaitingException(String message) {
        super(message);
    }
}
