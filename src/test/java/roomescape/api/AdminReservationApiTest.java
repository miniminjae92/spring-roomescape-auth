package roomescape.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.Store;
import roomescape.global.auth.SessionManager;
import roomescape.util.ApiTestSupport;
import roomescape.util.TestDataInitializer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationApiTest extends ApiTestSupport {

    @Autowired
    private TestDataInitializer dataInitializer;

    @Test
    void 전체_예약_목록을_조회한다() {
        String managerSessionId = createManagerSession();

        RestAssured.given().log().all()
                .cookie(SessionManager.SESSION_COOKIE_NAME, managerSessionId)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    @Test
    void Authorization_헤더로_관리자_예약_목록을_조회한다() {
        String managerSessionId = createManagerMobileSession();

        RestAssured.given().log().all()
                .header("Authorization", managerSessionId)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    @Test
    void 쿠키와_Authorization_헤더를_함께_전달하면_관리자_예약_목록을_조회할_수_없다() {
        String cookieSessionId = createManagerSession();
        String authorizationSessionId = loginMobile("manager", "password");

        RestAssured.given().log().all()
                .cookie(SessionManager.SESSION_COOKIE_NAME, cookieSessionId)
                .header("Authorization", authorizationSessionId)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(400)
                .body("message", is("인증 정보는 하나만 전달해주세요."));
    }

    @Test
    void 예약을_하드_삭제한다() {
        String managerSessionId = createManagerSession();
        dataInitializer.createReservationTime(LocalTime.now());
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        createMemberReservation("고래", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .cookie(SessionManager.SESSION_COOKIE_NAME, managerSessionId)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie(SessionManager.SESSION_COOKIE_NAME, managerSessionId)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1, 200",
            "0, 0, 400",
            "0, 100, 200",
            "0, 101, 400",
            "-1, 20, 400"
    })
    void 예약_목록_페이징_조건의_경계값을_검증한다(int page, int size, int statusCode) {
        String managerSessionId = createManagerSession();

        RestAssured.given().log().all()
                .cookie(SessionManager.SESSION_COOKIE_NAME, managerSessionId)
                .queryParam("page", page)
                .queryParam("size", size)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(statusCode);
    }

    @Test
    void 로그인하지_않으면_관리자_예약_목록을_조회할_수_없다() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(401)
                .body("message", is("인증이 필요합니다."));
    }

    @Test
    void 일반_회원은_관리자_예약_목록을_조회할_수_없다() {
        String userSessionId = createUserSession();

        RestAssured.given().log().all()
                .cookie(SessionManager.SESSION_COOKIE_NAME, userSessionId)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(403)
                .body("message", is("관리자 권한이 필요합니다."));
    }

    @Test
    void 관리자는_자신이_관리하는_매장_예약만_조회한다() {
        Member manager = dataInitializer.createManager("manager", "password", "관리자");
        dataInitializer.createStoreManager(1L, manager.getId());
        String managerSessionId = login("manager", "password");

        Store otherStore = dataInitializer.createStore("다른 매장");
        dataInitializer.createReservationTime(LocalTime.of(10, 0));
        dataInitializer.createReservationTime(LocalTime.of(11, 0));
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        createMemberReservation("고래", LocalDate.now().plusDays(1), 1L, 1L);
        createMemberReservation(otherStore.getId(), "상어", LocalDate.now().plusDays(1), 2L, 1L);

        RestAssured.given().log().all()
                .cookie(SessionManager.SESSION_COOKIE_NAME, managerSessionId)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", is("고래"));
    }

    @Test
    void 다른_매장의_예약은_삭제할_수_없다() {
        Member manager = dataInitializer.createManager("manager", "password", "관리자");
        dataInitializer.createStoreManager(1L, manager.getId());
        String managerSessionId = login("manager", "password");

        Store otherStore = dataInitializer.createStore("다른 매장");
        dataInitializer.createReservationTime(LocalTime.of(10, 0));
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        createMemberReservation(otherStore.getId(), "상어", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .cookie(SessionManager.SESSION_COOKIE_NAME, managerSessionId)
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(403)
                .body("message", is("접근 권한이 없습니다."));
    }

    private void createMemberReservation(String name, LocalDate date, Long timeId, Long themeId) {
        createMemberReservation(1L, name, date, timeId, themeId);
    }

    private void createMemberReservation(Long storeId, String name, LocalDate date, Long timeId, Long themeId) {
        Member member = dataInitializer.createMember("member-" + name + "-" + date + "-" + timeId + "-" + themeId, "password", name);
        dataInitializer.createMemberReservation(storeId, member.getId(), member.getName(), date, timeId, themeId);
    }

    private String createManagerSession() {
        Member manager = dataInitializer.createManager("manager", "password", "관리자");
        dataInitializer.createStoreManager(1L, manager.getId());
        return login("manager", "password");
    }

    private String createManagerMobileSession() {
        Member manager = dataInitializer.createManager("manager", "password", "관리자");
        dataInitializer.createStoreManager(1L, manager.getId());
        return loginMobile("manager", "password");
    }

    private String createUserSession() {
        dataInitializer.createMember("user", "password", "사용자");
        return login("user", "password");
    }

    private String login(String loginId, String password) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("loginId", loginId, "password", password))
                .when().post("/login/web")
                .then().extract()
                .cookie(SessionManager.SESSION_COOKIE_NAME);
    }

    private String loginMobile(String loginId, String password) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(Map.of("loginId", loginId, "password", password))
                .when().post("/login/mobile")
                .then().extract()
                .path("sessionId");
    }
}
