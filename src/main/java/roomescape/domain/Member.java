package roomescape.domain;

import lombok.Getter;
import roomescape.global.exception.member.InvalidMemberException;

@Getter
public class Member {

    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 20;
    private static final String NAME_PATTERN = "^[가-힣a-zA-Z ]+$";

    private final Long id;
    private final String loginId;
    private final String password;
    private final String name;

    private Member(Long id, String loginId, String password, String name) {
        validateLoginId(loginId);
        validatePassword(password);
        validateName(name);
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
    }

    public static Member createNew(String loginId, String password, String name) {
        return new Member(null, loginId, password, name);
    }

    public static Member from(Long id, String loginId, String password, String name) {
        return new Member(id, loginId, password, name);
    }

    public boolean hasPassword(String password) {
        return this.password.equals(password);
    }

    private void validateLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            throw new InvalidMemberException("로그인 ID는 비어있을 수 없습니다.");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new InvalidMemberException("비밀번호는 비어있을 수 없습니다.");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidMemberException("이름은 비어있을 수 없습니다.");
        }
        if (name.length() < MIN_NAME_LENGTH) {
            throw new InvalidMemberException("이름은 " + MIN_NAME_LENGTH + "자 이상이어야 합니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidMemberException("이름은 " + MAX_NAME_LENGTH + "자 이하여야 합니다.");
        }
        if (!name.matches(NAME_PATTERN)) {
            throw new InvalidMemberException("이름은 완성형 한글, 영문, 공백만 허용합니다.");
        }
    }
}
