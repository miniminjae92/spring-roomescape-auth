package roomescape.global.auth;

import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "SESSION";

    private final Map<String, LoginMember> sessions = new ConcurrentHashMap<>();

    public Cookie createSession(Long memberId, String name) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new LoginMember(memberId, name));

        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
}
