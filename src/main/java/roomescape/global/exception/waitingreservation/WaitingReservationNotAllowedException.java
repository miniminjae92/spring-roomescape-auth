package roomescape.global.exception.waitingreservation;

import roomescape.global.exception.status.ConflictException;

public class WaitingReservationNotAllowedException extends ConflictException {

    public WaitingReservationNotAllowedException(String message) {
        super(message);
    }
}
