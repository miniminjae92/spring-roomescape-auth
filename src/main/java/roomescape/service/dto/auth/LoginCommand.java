package roomescape.service.dto.auth;

public record LoginCommand(
        String loginId,
        String password
) {
}
