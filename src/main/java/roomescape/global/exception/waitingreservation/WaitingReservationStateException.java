package roomescape.global.exception.waitingreservation;

import roomescape.global.exception.status.ConflictException;

public class WaitingReservationStateException extends ConflictException {

    public WaitingReservationStateException(String message) {
        super(message);
    }
}
