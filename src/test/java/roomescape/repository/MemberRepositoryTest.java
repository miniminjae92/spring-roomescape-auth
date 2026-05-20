package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 로그인_ID로_회원을_조회한다() {
        Member savedMember = memberRepository.save(Member.createNew("whale", "password", "고래"));

        Optional<Member> member = memberRepository.findByLoginId("whale");

        assertThat(member).isPresent();
        assertThat(member.get().getId()).isEqualTo(savedMember.getId());
        assertThat(member.get().getName()).isEqualTo("고래");
    }

    @Test
    void 로그인_ID에_해당하는_회원이_없으면_빈_값을_반환한다() {
        memberRepository.save(Member.createNew("whale", "password", "고래"));

        Optional<Member> member = memberRepository.findByLoginId("shark");

        assertThat(member).isEmpty();
    }

    @Test
    void 식별자로_회원을_조회한다() {
        Member savedMember = memberRepository.save(Member.createNew("whale", "password", "고래"));

        Optional<Member> member = memberRepository.findById(savedMember.getId());

        assertThat(member).isPresent();
        assertThat(member.get().getId()).isEqualTo(savedMember.getId());
    }

}
