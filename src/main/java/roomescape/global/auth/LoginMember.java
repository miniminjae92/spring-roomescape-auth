package roomescape.global.auth;

import roomescape.domain.MemberRole;

public record LoginMember(
        Long id,
        String name,
        MemberRole role
) {
}
