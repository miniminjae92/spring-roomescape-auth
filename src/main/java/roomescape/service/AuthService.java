package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.global.exception.auth.LoginFailedException;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.auth.LoginCommand;
import roomescape.service.dto.auth.LoginResult;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;

    public LoginResult login(LoginCommand command) {
        Member member = memberRepository.findByLoginId(command.loginId())
                .orElseThrow(() -> new LoginFailedException("잘못된 정보입니다. 다시 시도해주세요."));
        validatePassword(member, command.password());
        return LoginResult.from(member);
    }

    private void validatePassword(Member member, String password) {
        if (!member.hasPassword(password)) {
            throw new LoginFailedException("잘못된 정보입니다. 다시 시도해주세요.");
        }
    }
}
