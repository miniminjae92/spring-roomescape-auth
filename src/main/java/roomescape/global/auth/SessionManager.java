package roomescape.global.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import roomescape.global.exception.auth.DuplicateAuthenticationException;

@Component
public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "SESSION";
    public static final String LOGIN_MEMBER_ATTRIBUTE = "loginMember";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final SessionStore sessionStore;

    public SessionManager(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public String createSession(LoginMember loginMember) {
        return sessionStore.create(loginMember);
    }

    public String createSessionCookie(String sessionId) {
        return ResponseCookie.from(SESSION_COOKIE_NAME, sessionId)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .build()
                .toString();
    }

    public Optional<LoginMember> findLoginMember(HttpServletRequest request) {
        return findSessionId(request)
                .flatMap(sessionStore::findBySessionId);
    }

    public Optional<String> findSessionId(HttpServletRequest request) {
        return extractSessionId(request);
    }

    public String expireSessionCookie() {
        return ResponseCookie.from(SESSION_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build()
                .toString();
    }

    public void invalidate(HttpServletRequest request) {
        extractSessionId(request).ifPresent(sessionStore::delete);
    }

    private Optional<String> extractSessionId(HttpServletRequest request) {
        Optional<String> authorizationSessionId = extractAuthorizationSessionId(request);
        Optional<String> cookieSessionId = extractCookieSessionId(request);
        if (authorizationSessionId.isPresent() && cookieSessionId.isPresent()) {
            throw new DuplicateAuthenticationException("인증 정보는 하나만 전달해주세요.");
        }

        return authorizationSessionId.or(() -> cookieSessionId);
    }

    private Optional<String> extractAuthorizationSessionId(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || authorization.isBlank()) {
            return Optional.empty();
        }
        String sessionId = authorization.trim();
        if (sessionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(sessionId);
    }

    private Optional<String> extractCookieSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> SESSION_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
