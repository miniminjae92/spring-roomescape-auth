package roomescape.global.exception.waitingreservation;

import roomescape.global.exception.status.BadRequestException;

public class InvalidWaitingReservationException extends BadRequestException {

    public InvalidWaitingReservationException(String message) {
        super(message);
    }
}
