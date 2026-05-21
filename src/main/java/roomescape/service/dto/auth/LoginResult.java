package roomescape.service.dto.auth;

import roomescape.domain.Member;
import roomescape.domain.MemberRole;

public record LoginResult(
        Long id,
        String loginId,
        String name,
        MemberRole role
) {

    public static LoginResult from(Member member) {
        return new LoginResult(member.getId(), member.getLoginId(), member.getName(), member.getRole());
    }
}
