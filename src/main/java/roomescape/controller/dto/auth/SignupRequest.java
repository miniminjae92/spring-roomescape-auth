package roomescape.controller.dto.auth;

import jakarta.validation.constraints.NotBlank;
import roomescape.service.dto.auth.SignupCommand;

public record SignupRequest(
        @NotBlank(message = "로그인 ID는 필수입니다.")
        String loginId,

        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {

    public SignupCommand toCommand() {
        return new SignupCommand(loginId, password, name);
    }
}
