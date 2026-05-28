package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.member.InvalidMemberException;

class MemberTest {

    @Test
    @DisplayName("회원을 정상적으로 생성한다.")
    void createMember() {
        Member member = Member.createUser("whale", "password", "고래");

        assertThat(member.getId()).isNull();
        assertThat(member.getLoginId()).isEqualTo("whale");
        assertThat(member.getName()).isEqualTo("고래");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("로그인 ID가 null이거나 비어있으면 예외가 발생한다.")
    void validateLoginId_NullOrBlank(String invalidLoginId) {
        assertThatThrownBy(() -> Member.createUser(invalidLoginId, "password", "고래"))
                .isInstanceOf(InvalidMemberException.class)
                .hasMessage("로그인 ID는 비어있을 수 없습니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("비밀번호가 null이거나 비어있으면 예외가 발생한다.")
    void validatePassword_NullOrBlank(String invalidPassword) {
        assertThatThrownBy(() -> Member.createUser("whale", invalidPassword, "고래"))
                .isInstanceOf(InvalidMemberException.class)
                .hasMessage("비밀번호는 비어있을 수 없습니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("이름이 null이거나 비어있으면 예외가 발생한다.")
    void validateName_NullOrBlank(String invalidName) {
        assertThatThrownBy(() -> Member.createUser("whale", "password", invalidName))
                .isInstanceOf(InvalidMemberException.class)
                .hasMessage("이름은 비어있을 수 없습니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"A", "가"})
    @DisplayName("이름이 2자 미만이면 예외가 발생한다.")
    void validateName_MinLength(String invalidName) {
        assertThatThrownBy(() -> Member.createUser("whale", "password", invalidName))
                .isInstanceOf(InvalidMemberException.class)
                .hasMessageContaining("2자 이상");
    }

    @Test
    @DisplayName("이름이 20자를 초과하면 예외가 발생한다.")
    void validateName_MaxLength() {
        String invalidName = "a".repeat(21);

        assertThatThrownBy(() -> Member.createUser("whale", "password", invalidName))
                .isInstanceOf(InvalidMemberException.class)
                .hasMessageContaining("20자 이하");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Brown1", "브라운!", "Brown_Lee"})
    @DisplayName("이름에 완성형 한글, 영문, 공백 외 문자가 포함되면 예외가 발생한다.")
    void validateName_AllowedCharacters(String invalidName) {
        assertThatThrownBy(() -> Member.createUser("whale", "password", invalidName))
                .isInstanceOf(InvalidMemberException.class)
                .hasMessageContaining("완성형 한글, 영문, 공백");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Brown", "브라운", "Brown Lee", "브라운 리"})
    @DisplayName("이름이 완성형 한글, 영문, 공백으로만 이루어지면 회원이 정상 생성된다.")
    void validateName_AllowedCharactersPass(String validName) {
        assertThatCode(() -> Member.createUser("whale", "password", validName))
                .doesNotThrowAnyException();
    }
}
