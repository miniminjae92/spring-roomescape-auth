package roomescape.service.dto.auth;

public record SignupCommand(
        String loginId,
        String password,
        String name
) {
}
