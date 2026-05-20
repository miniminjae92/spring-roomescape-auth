package roomescape.service.dto.auth;

import roomescape.domain.Member;

public record LoginResult(
        Long id,
        String loginId,
        String name
) {

    public static LoginResult from(Member member) {
        return new LoginResult(member.getId(), member.getLoginId(), member.getName());
    }
}
