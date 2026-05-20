package roomescape.global.auth;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemorySessionStore implements SessionStore {

    private final Map<String, LoginMember> sessions = new ConcurrentHashMap<>();

    @Override
    public String create(LoginMember loginMember) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, loginMember);
        return sessionId;
    }

    @Override
    public Optional<LoginMember> findBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public void delete(String sessionId) {
        sessions.remove(sessionId);
    }
}
