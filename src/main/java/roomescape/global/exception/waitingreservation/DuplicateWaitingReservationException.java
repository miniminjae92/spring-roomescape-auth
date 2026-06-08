package roomescape.global.exception.waitingreservation;

import roomescape.global.exception.status.ConflictException;

public class DuplicateWaitingReservationException extends ConflictException {

    public DuplicateWaitingReservationException(String message) {
        super(message);
    }
}
