package roomescape.global.exception.auth;

import roomescape.global.exception.status.BadRequestException;

public class DuplicateAuthenticationException extends BadRequestException {

    public DuplicateAuthenticationException(String message) {
        super(message);
    }
}
