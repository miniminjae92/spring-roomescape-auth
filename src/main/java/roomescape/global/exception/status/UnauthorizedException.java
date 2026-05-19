package roomescape.global.exception.status;

import org.springframework.http.HttpStatus;
import roomescape.global.exception.RoomescapeException;

public abstract class UnauthorizedException extends RoomescapeException {

    protected UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
