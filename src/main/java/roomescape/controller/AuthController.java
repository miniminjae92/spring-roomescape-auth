package roomescape.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.auth.LoginRequest;
import roomescape.controller.dto.auth.LoginResponse;
import roomescape.controller.dto.auth.SignupRequest;
import roomescape.global.auth.Authenticated;
import roomescape.global.auth.LoginMember;
import roomescape.global.auth.LoginRequired;
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
        addAuthSession(response, result);
        return ResponseEntity.ok(LoginResponse.from(result));
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(
            @Valid @RequestBody SignupRequest request,
            HttpServletResponse response
    ) {
        LoginResult result = authService.signup(request.toCommand());
        addAuthSession(response, result);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LoginResponse.from(result));
    }

    @GetMapping("/me")
    @LoginRequired
    public ResponseEntity<LoginResponse> me(@Authenticated LoginMember loginMember) {
        LoginResult result = authService.getById(loginMember.id());
        return ResponseEntity.ok(LoginResponse.from(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        sessionManager.invalidate(request);
        response.addCookie(sessionManager.expireSessionCookie());
        return ResponseEntity.noContent().build();
    }

    private void addAuthSession(HttpServletResponse response, LoginResult result) {
        Cookie cookie = sessionManager.createSession(new LoginMember(result.id(), result.name()));
        response.addCookie(cookie);
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + cookie.getValue());
    }
}
