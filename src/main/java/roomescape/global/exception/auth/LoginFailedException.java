package roomescape.global.exception.auth;

import roomescape.global.exception.status.UnauthorizedException;

public class LoginFailedException extends UnauthorizedException {

    public LoginFailedException(String message) {
        super(message);
    }
}
