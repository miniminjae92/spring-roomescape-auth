package roomescape.global.exception.store;

import roomescape.global.exception.status.BadRequestException;

public class InvalidStoreException extends BadRequestException {

    public InvalidStoreException(String message) {
        super(message);
    }
}
