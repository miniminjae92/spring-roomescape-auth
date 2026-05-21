package roomescape.api;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

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
                .when().post("/login/web")
                .then().log().all()
                .statusCode(200)
                .cookie(SessionManager.SESSION_COOKIE_NAME, notNullValue())
                .header("Set-Cookie", allOf(
                        containsString("HttpOnly"),
                        containsString("Secure"),
                        containsString("SameSite=Lax")
                ))
                .header("Authorization", nullValue())
                .body("name", is("고래"));
    }

    @Test
    void 모바일_로그인은_세션_ID를_응답_본문에_담아_반환한다() {
        dataInitializer.createMember("whale", "password", "고래");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams("whale", "password"))
                .when().post("/login/mobile")
                .then().log().all()
                .statusCode(200)
                .header("Set-Cookie", nullValue())
                .header("Authorization", nullValue())
                .body("sessionId", notNullValue());
    }

    @Test
    void 회원가입한다() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signupParams("whale", "고래", "password"))
                .when().post("/signup")
                .then().log().all()
                .statusCode(201)
                .cookie(SessionManager.SESSION_COOKIE_NAME, notNullValue())
                .header("Authorization", nullValue())
                .body("loginId", is("whale"))
                .body("name", is("고래"));
    }

    @Test
    void 이미_가입된_로그인_ID면_409를_반환한다() {
        dataInitializer.createMember("whale", "password", "고래");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(signupParams("whale", "상어", "password"))
                .when().post("/signup")
                .then().log().all()
                .statusCode(409)
                .body("message", is("이미 가입된 로그인 ID입니다."));
    }

    @Test
    void 존재하지_않는_로그인_ID이면_401을_반환한다() {
        dataInitializer.createMember("whale", "password", "고래");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginParams("shark", "password"))
                .when().post("/login/web")
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
                .when().post("/login/web")
                .then().log().all()
                .statusCode(401)
                .body("message", is("잘못된 정보입니다. 다시 시도해주세요."));
    }

    @Test
    void 로그인한_사용자는_me를_조회할_수_있다() {
        dataInitializer.createMember("whale", "password", "고래");
        String sessionId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginParams("whale", "password"))
                .when().post("/login/web")
                .then().extract().cookie(SessionManager.SESSION_COOKIE_NAME);

        RestAssured.given().log().all()
                .cookie(SessionManager.SESSION_COOKIE_NAME, sessionId)
                .when().get("/me")
                .then().log().all()
                .statusCode(200)
                .body("loginId", is("whale"))
                .body("name", is("고래"));
    }

    @Test
    void 로그인하지_않으면_me_조회_시_401을_반환한다() {
        RestAssured.given().log().all()
                .when().get("/me")
                .then().log().all()
                .statusCode(401)
                .body("message", is("인증이 필요합니다."));
    }

    private Map<String, Object> loginParams(String loginId, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginId", loginId);
        params.put("password", password);
        return params;
    }

    private Map<String, Object> signupParams(String loginId, String name, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginId", loginId);
        params.put("name", name);
        params.put("password", password);
        return params;
    }
}
