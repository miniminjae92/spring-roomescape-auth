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
    private final MemberRole role;

    private Member(Long id, String loginId, String password, String name, MemberRole role) {
        validateLoginId(loginId);
        validatePassword(password);
        validateName(name);
        validateRole(role);
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public static Member createUser(String loginId, String password, String name) {
        return new Member(null, loginId, password, name, MemberRole.USER);
    }

    public static Member createManager(String loginId, String password, String name) {
        return new Member(null, loginId, password, name, MemberRole.MANAGER);
    }

    public static Member from(Long id, String loginId, String password, String name, MemberRole role) {
        return new Member(id, loginId, password, name, role);
    }

    public boolean matchesPassword(String password) {
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

    private void validateRole(MemberRole role) {
        if (role == null) {
            throw new InvalidMemberException("회원 역할은 필수입니다.");
        }
    }
}
