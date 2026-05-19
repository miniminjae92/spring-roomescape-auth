package roomescape.global.exception.status;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomescapeException;

public abstract class ForbiddenException extends RoomescapeException {

    protected ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
