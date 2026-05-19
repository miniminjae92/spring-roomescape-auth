package roomescape.controller.dto.auth;

import jakarta.validation.constraints.NotBlank;
import roomescape.service.dto.auth.LoginCommand;

public record LoginRequest(
        @NotBlank(message = "로그인 ID는 필수입니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(loginId, password);
    }
}
