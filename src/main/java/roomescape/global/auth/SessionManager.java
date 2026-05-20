package roomescape.global.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "SESSION";
    public static final String LOGIN_MEMBER_ATTRIBUTE = "loginMember";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SessionStore sessionStore;

    public SessionManager(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    public Cookie createSession(LoginMember loginMember) {
        String sessionId = sessionStore.create(loginMember);

        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public Optional<LoginMember> findLoginMember(HttpServletRequest request) {
        return findSessionId(request)
                .flatMap(sessionStore::findBySessionId);
    }

    public Optional<String> findSessionId(HttpServletRequest request) {
        return extractSessionId(request);
    }

    public Cookie expireSessionCookie() {
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        return cookie;
    }

    public void invalidate(HttpServletRequest request) {
        extractSessionId(request).ifPresent(sessionStore::delete);
    }

    private Optional<String> extractSessionId(HttpServletRequest request) {
        Optional<String> authorizationSessionId = extractBearerSessionId(request);
        if (authorizationSessionId.isPresent()) {
            return authorizationSessionId;
        }

        return extractCookieSessionId(request);
    }

    private Optional<String> extractBearerSessionId(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || authorization.isBlank()) {
            return Optional.empty();
        }
        if (!authorization.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        String sessionId = authorization.substring(BEARER_PREFIX.length()).trim();
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
