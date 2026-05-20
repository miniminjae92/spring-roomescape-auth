package roomescape.global.auth;

import java.util.Optional;

public interface SessionStore {

    String create(LoginMember loginMember);

    Optional<LoginMember> findBySessionId(String sessionId);

    void delete(String sessionId);
}
