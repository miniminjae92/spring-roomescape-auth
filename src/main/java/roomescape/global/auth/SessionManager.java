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
