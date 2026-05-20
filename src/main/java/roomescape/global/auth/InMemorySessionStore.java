package roomescape.global.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InMemorySessionStore implements SessionStore {

    private static final Duration SESSION_TTL = Duration.ofHours(12);
    private final Map<String, SessionEntry> sessions = new ConcurrentHashMap<>();

    @Override
    public String create(LoginMember loginMember) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new SessionEntry(loginMember, Instant.now()));
        return sessionId;
    }

    @Override
    public Optional<LoginMember> findBySessionId(String sessionId) {
        SessionEntry entry = sessions.get(sessionId);
        if (entry == null) {
            return Optional.empty();
        }
        if (isExpired(entry)) {
            sessions.remove(sessionId);
            return Optional.empty();
        }
        return Optional.of(entry.loginMember());
    }

    @Override
    public void delete(String sessionId) {
        sessions.remove(sessionId);
    }

    @Scheduled(fixedDelay = 600_000L)
    void purgeExpiredSessions() {
        sessions.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    private boolean isExpired(SessionEntry entry) {
        return entry.createdAt().plus(SESSION_TTL).isBefore(Instant.now());
    }

    private record SessionEntry(LoginMember loginMember, Instant createdAt) {
    }
}
