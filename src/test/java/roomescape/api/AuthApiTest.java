package roomescape.api;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.auth.SessionManager;
import roomescape.util.ApiTestSupport;
import roomescape.util.TestDataInitializer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthApiTest extends ApiTestSupport {

    @Autowired
    private TestDataInitializer dataInitializer;

    @Test
    void 로그인한다() {
        dataInitializer.createMember("whale", "password", "고래");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams("whale", "password"))
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .cookie(SessionManager.SESSION_COOKIE_NAME, notNullValue())
                .body("name", is("고래"));
    }

    @Test
    void 존재하지_않는_로그인_ID이면_401을_반환한다() {
        dataInitializer.createMember("whale", "password", "고래");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams("shark", "password"))
                .when().post("/login")
                .then().log().all()
                .statusCode(401)
                .body("message", is("잘못된 정보입니다. 다시 시도해주세요."));
    }

    @Test
    void 비밀번호가_일치하지_않으면_401을_반환한다() {
        dataInitializer.createMember("whale", "password", "고래");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams("whale", "wrong"))
                .when().post("/login")
                .then().log().all()
                .statusCode(401)
                .body("message", is("잘못된 정보입니다. 다시 시도해주세요."));
    }

    private Map<String, Object> loginParams(String loginId, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginId", loginId);
        params.put("password", password);
        return params;
    }
}
