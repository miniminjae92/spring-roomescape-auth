package roomescape.controller.dto.auth;

import roomescape.service.dto.auth.LoginResult;

public record LoginResponse(
        Long id,
        String loginId,
        String name
) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.id(), result.loginId(), result.name());
    }
}
