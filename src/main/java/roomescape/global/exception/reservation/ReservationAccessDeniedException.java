package roomescape.global.exception.reservation;

import roomescape.global.exception.status.ForbiddenException;

public class ReservationAccessDeniedException extends ForbiddenException {

    public ReservationAccessDeniedException(String message) {
        super(message);
    }
}
