package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.global.exception.auth.LoginFailedException;
import roomescape.global.exception.member.DuplicateMemberException;
import roomescape.global.exception.member.MemberNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.auth.LoginCommand;
import roomescape.service.dto.auth.LoginResult;
import roomescape.service.dto.auth.SignupCommand;

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

    public LoginResult signup(SignupCommand command) {
        if (memberRepository.findByLoginId(command.loginId()).isPresent()) {
            throw new DuplicateMemberException("이미 가입된 로그인 ID입니다.");
        }
        Member member = memberRepository.save(Member.createUser(command.loginId(), command.password(), command.name()));
        return LoginResult.from(member);
    }

    public LoginResult getById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다."));
        return LoginResult.from(member);
    }

    private void validatePassword(Member member, String password) {
        if (!member.matchesPassword(password)) {
            throw new LoginFailedException("잘못된 정보입니다. 다시 시도해주세요.");
        }
    }
}
