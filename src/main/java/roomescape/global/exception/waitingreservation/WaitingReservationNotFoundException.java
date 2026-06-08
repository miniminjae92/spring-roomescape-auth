package roomescape.global.exception.waitingreservation;

import roomescape.global.exception.status.NotFoundException;

public class WaitingReservationNotFoundException extends NotFoundException {

    public WaitingReservationNotFoundException(String message) {
        super(message);
    }
}
