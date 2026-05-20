package roomescape.global.exception.member;

import roomescape.global.exception.status.ConflictException;

public class DuplicateMemberException extends ConflictException {

    public DuplicateMemberException(String message) {
        super(message);
    }
}
