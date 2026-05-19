package roomescape.global.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "SESSION";
    public static final String LOGIN_MEMBER_ATTRIBUTE = "loginMember";

    private final Map<String, LoginMember> sessions = new ConcurrentHashMap<>();

    public Cookie createSession(Long memberId, String name) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new LoginMember(memberId, name));

        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }

    public Optional<LoginMember> findLoginMember(HttpServletRequest request) {
        return findSessionId(request)
                .map(sessions::get);
    }

    private Optional<String> findSessionId(HttpServletRequest request) {
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
