package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.exception.auth.LoginFailedException;
import roomescape.service.dto.auth.LoginCommand;
import roomescape.service.dto.auth.LoginResult;
import roomescape.util.TestDataInitializer;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TestDataInitializer dataInitializer;

    @Test
    void 로그인에_성공하면_회원_정보를_반환한다() {
        dataInitializer.createMember("whale", "password", "고래");

        LoginResult result = authService.login(new LoginCommand("whale", "password"));

        assertThat(result.name()).isEqualTo("고래");
    }

    @Test
    void 존재하지_않는_로그인_ID로_로그인하면_예외가_발생한다() {
        dataInitializer.createMember("whale", "password", "고래");

        assertThatThrownBy(() -> authService.login(new LoginCommand("shark", "password")))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("잘못된 정보입니다. 다시 시도해주세요.");
    }

    @Test
    void 비밀번호가_일치하지_않으면_예외가_발생한다() {
        dataInitializer.createMember("whale", "password", "고래");

        assertThatThrownBy(() -> authService.login(new LoginCommand("whale", "wrong")))
                .isInstanceOf(LoginFailedException.class)
                .hasMessage("잘못된 정보입니다. 다시 시도해주세요.");
    }
}
