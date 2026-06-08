package roomescape.global.exception.waitingreservation;

import roomescape.global.exception.status.ForbiddenException;

public class WaitingReservationAccessDeniedException extends ForbiddenException {

    public WaitingReservationAccessDeniedException(String message) {
        super(message);
    }
}
