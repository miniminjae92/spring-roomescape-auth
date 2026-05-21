package roomescape.global.exception.auth;

import roomescape.global.exception.status.ForbiddenException;

public class AuthorizationFailedException extends ForbiddenException {

    public AuthorizationFailedException(String message) {
        super(message);
    }
}
