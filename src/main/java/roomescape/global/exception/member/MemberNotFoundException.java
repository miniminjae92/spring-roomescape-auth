package roomescape.global.exception.member;

import roomescape.global.exception.status.NotFoundException;

public class MemberNotFoundException extends NotFoundException {

    public MemberNotFoundException(String message) {
        super(message);
    }
}
