package roomescape.global.exception.member;

import roomescape.global.exception.status.BadRequestException;

public class InvalidMemberException extends BadRequestException {

    public InvalidMemberException(String message) {
        super(message);
    }
}
