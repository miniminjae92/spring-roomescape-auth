package roomescape.global.exception.auth;

import roomescape.global.exception.status.UnauthorizedException;

public class AuthenticationRequiredException extends UnauthorizedException {

    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
