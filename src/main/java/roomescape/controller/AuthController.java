package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.auth.LoginRequest;
import roomescape.controller.dto.auth.LoginResponse;
import roomescape.global.auth.SessionManager;
import roomescape.service.AuthService;
import roomescape.service.dto.auth.LoginResult;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionManager sessionManager;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResult result = authService.login(request.toCommand());
        Cookie cookie = sessionManager.createSession(result.id(), result.name());
        response.addCookie(cookie);
        return ResponseEntity.ok(LoginResponse.from(result));
    }
}
