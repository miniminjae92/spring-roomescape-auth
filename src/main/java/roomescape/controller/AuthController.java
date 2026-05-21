package roomescape.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.auth.LoginRequest;
import roomescape.controller.dto.auth.LoginResponse;
import roomescape.controller.dto.auth.MobileLoginResponse;
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

    @PostMapping("/login/web")
    public ResponseEntity<LoginResponse> loginWeb(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResult result = authService.login(request.toCommand());
        String sessionId = sessionManager.createSession(new LoginMember(result.id(), result.name()));
        response.addHeader(HttpHeaders.SET_COOKIE, sessionManager.createSessionCookie(sessionId));
        return ResponseEntity.ok(LoginResponse.from(result));
    }

    @PostMapping("/login/mobile")
    public ResponseEntity<MobileLoginResponse> loginMobile(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.toCommand());
        String sessionId = sessionManager.createSession(new LoginMember(result.id(), result.name()));
        return ResponseEntity.ok(new MobileLoginResponse(sessionId));
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(
            @Valid @RequestBody SignupRequest request,
            HttpServletResponse response
    ) {
        LoginResult result = authService.signup(request.toCommand());
        String sessionId = sessionManager.createSession(new LoginMember(result.id(), result.name()));
        response.addHeader(HttpHeaders.SET_COOKIE, sessionManager.createSessionCookie(sessionId));
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
        response.addHeader(HttpHeaders.SET_COOKIE, sessionManager.expireSessionCookie());
        return ResponseEntity.noContent().build();
    }
}
