package roomescape.global.exception.store;

import roomescape.global.exception.status.NotFoundException;

public class StoreNotFoundException extends NotFoundException {

    public StoreNotFoundException(String message) {
        super(message);
    }
}
