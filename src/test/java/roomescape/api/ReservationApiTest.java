package roomescape.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.global.auth.SessionManager;
import roomescape.util.ApiTestSupport;
import roomescape.util.TestDataInitializer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationApiTest extends ApiTestSupport {

    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private TestDataInitializer dataInitializer;

    private Member loginMember;

    @BeforeEach
    void setUpAuthenticatedRequest() {
        loginMember = dataInitializer.createMember("whale", "password", "고래");
        String sessionId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginParams("whale", "password"))
                .when().post("/login")
                .then().extract()
                .cookie(SessionManager.SESSION_COOKIE_NAME);
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addCookie(SessionManager.SESSION_COOKIE_NAME, sessionId)
                .build();
    }

    @Test
    void 로그인_사용자는_본인의_예약_목록을_조회할_수_있다() {
        dataInitializer.createReservationTime(LocalTime.of(10, 0));
        dataInitializer.createReservationTime(LocalTime.of(11, 0));
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(1), 1L, 1L);
        createMemberReservation("라텔", TODAY.plusDays(1), 2L, 1L);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", is("고래"));
    }

    @Test
    void Authorization_Bearer_헤더로도_예약_목록을_조회할_수_있다() {
        RestAssured.requestSpecification = null;
        dataInitializer.createReservationTime(LocalTime.of(10, 0));
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(1), 1L, 1L);

        String sessionId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(loginParams("whale", "password"))
                .when().post("/login")
                .then().extract()
                .cookie(SessionManager.SESSION_COOKIE_NAME);

        RestAssured.given().log().all()
                .header("Authorization", "Bearer " + sessionId)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].name", is("고래"));
    }

    @Test
    void 로그인_사용자는_본인의_예약_이력을_페이징_조회할_수_있다() {
        dataInitializer.createReservationTime(LocalTime.of(10, 0));
        dataInitializer.createReservationTime(LocalTime.of(11, 0));
        dataInitializer.createReservationTime(LocalTime.of(12, 0));
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(1), 1L, 1L);
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(2), 2L, 1L);
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(3), 3L, 1L);

        RestAssured.given().log().all()
                .queryParam("page", 1)
                .queryParam("size", 2)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].id", is(3));
    }

    @Test
    void 예약을_생성한다() {
        dataInitializer.createReservationTime(LocalTime.now());
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(1).toString());
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(1));

    }

    @Test
    void 로그인하지_않고_예약을_생성하면_401을_반환한다() {
        RestAssured.requestSpecification = null;
        createReservationPrerequisites(LocalTime.of(10, 0));

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(1).toString());
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401)
                .body("message", is("인증이 필요합니다."));
    }

    @Test
    void 로그인하지_않고_예약_목록을_조회하면_401을_반환한다() {
        RestAssured.requestSpecification = null;

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(401)
                .body("message", is("인증이 필요합니다."));
    }

    @Test
    void 로그인하지_않고_예약을_변경하면_401을_반환한다() {
        RestAssured.requestSpecification = null;
        createReservationPrerequisites(LocalTime.of(10, 0));

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(2).toString());
        params.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().put("/reservations/1/schedule")
                .then().log().all()
                .statusCode(401)
                .body("message", is("인증이 필요합니다."));
    }

    @Test
    void 로그인하지_않고_예약을_취소하면_401을_반환한다() {
        RestAssured.requestSpecification = null;

        RestAssured.given().log().all()
                .when().post("/reservations/1/cancellations")
                .then().log().all()
                .statusCode(401)
                .body("message", is("인증이 필요합니다."));
    }

    @Test
    void 예약_날짜가_null이면_400을_반환한다() {
        createReservationPrerequisites(LocalTime.of(10, 0));

        Map<String, Object> params = new HashMap<>();
        params.put("date", null);
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약_날짜가_yyyy_MM_dd_형식이_아니면_400을_반환한다() {
        createReservationPrerequisites(LocalTime.of(10, 0));

        Map<String, Object> params = new HashMap<>();
        params.put("date", "2026/05/20");
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약_시간_식별자가_null이면_400을_반환한다() {
        createReservationPrerequisites(LocalTime.of(10, 0));

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(1).toString());
        params.put("timeId", null);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 테마_식별자가_null이면_400을_반환한다() {
        createReservationPrerequisites(LocalTime.of(10, 0));

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(1).toString());
        params.put("timeId", 1);
        params.put("themeId", null);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 존재하지_않는_예약_시간으로_예약을_생성하면_404를_반환한다() {
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(1).toString());
        params.put("timeId", 999);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 존재하지_않는_테마로_예약을_생성하면_404를_반환한다() {
        dataInitializer.createReservationTime(LocalTime.of(10, 0));

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(1).toString());
        params.put("timeId", 1);
        params.put("themeId", 999);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 같은_날짜_시간_테마로_중복_예약하면_409를_반환한다() {
        dataInitializer.createReservationTime(LocalTime.of(10, 0));
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        createMemberReservation("고래", TODAY.plusDays(1), 1L, 1L);

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(1).toString());
        params.put("timeId", 1);
        params.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 로그인_사용자는_본인_예약의_날짜와_시간을_변경할_수_있다() {
        dataInitializer.createReservationTime(LocalTime.of(10, 0));
        dataInitializer.createReservationTime(LocalTime.of(11, 0));
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(1), 1L, 1L);

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(2).toString());
        params.put("timeId", 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().put("/reservations/1/schedule")
                .then().log().all()
                .statusCode(200)
                .body("date", is(TODAY.plusDays(2).toString()))
                .body("time.id", is(2))
                .body("status", is("RESERVED"));
    }

    @Test
    void 로그인_사용자는_본인_예약을_취소할_수_있다() {
        dataInitializer.createReservationTime(LocalTime.of(10, 0));
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .when().post("/reservations/1/cancellations")
                .then().log().all()
                .statusCode(200)
                .body("status", is("CANCELLED"));
    }

    @Test
    void 사용자는_취소한_예약과_같은_슬롯으로_다시_예약할_수_있다() {
        createReservationPrerequisites(LocalTime.of(10, 0));
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .when().post("/reservations/1/cancellations")
                .then().log().all()
                .statusCode(200)
                .body("status", is("CANCELLED"));

        createReservationRequest(TODAY.plusDays(1), 1L, 1L)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("id", is(2))
                .body("status", is("RESERVED"));
    }

    @Test
    void 존재하지_않는_예약을_변경하면_404를_반환한다() {
        createReservationPrerequisites(LocalTime.of(10, 0));

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(2).toString());
        params.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().put("/reservations/999/schedule")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 존재하지_않는_예약을_취소하면_404를_반환한다() {
        RestAssured.given().log().all()
                .when().post("/reservations/999/cancellations")
                .then().log().all()
                .statusCode(404);
    }

    @Test
    void 회원이_일치하지_않으면_403을_반환한다() {
        createReservationPrerequisites(LocalTime.of(10, 0));
        Member otherMember = dataInitializer.createMember("shark", "password", "상어");
        dataInitializer.createMemberReservation(otherMember.getId(), "상어", TODAY.plusDays(1), 1L, 1L);

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(2).toString());
        params.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().put("/reservations/1/schedule")
                .then().log().all()
                .statusCode(403)
                .body("message", is("접근 권한이 없습니다."));
    }

    @Test
    void 이미_예약된_슬롯으로_예약을_변경하면_409를_반환한다() {
        dataInitializer.createReservationTime(LocalTime.of(10, 0));
        dataInitializer.createReservationTime(LocalTime.of(11, 0));
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(1), 1L, 1L);
        createMemberReservation("라텔", TODAY.plusDays(1), 2L, 1L);

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(1).toString());
        params.put("timeId", 2);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().put("/reservations/1/schedule")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 이미_같은_일정으로_예약되어_있으면_409를_반환한다() {
        createReservationPrerequisites(LocalTime.of(10, 0));
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(1), 1L, 1L);

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(1).toString());
        params.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().put("/reservations/1/schedule")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 지난_예약을_변경하면_409를_반환한다() {
        createReservationPrerequisites(LocalTime.of(10, 0));
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.minusDays(1), 1L, 1L);

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(1).toString());
        params.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().put("/reservations/1/schedule")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 지난_예약을_취소하면_409를_반환한다() {
        createReservationPrerequisites(LocalTime.of(10, 0));
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.minusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .when().post("/reservations/1/cancellations")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 이미_취소된_예약을_변경하면_409를_반환한다() {
        createCancelledReservation();

        Map<String, Object> params = new HashMap<>();
        params.put("date", TODAY.plusDays(2).toString());
        params.put("timeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().put("/reservations/1/schedule")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 이미_취소된_예약을_다시_취소하면_409를_반환한다() {
        createCancelledReservation();

        RestAssured.given().log().all()
                .when().post("/reservations/1/cancellations")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    void 지나간_날짜와_시간으로_예약하면_400을_반환한다() {
        createReservationPrerequisites(LocalTime.of(15, 0));

        createReservationRequest(TODAY.minusDays(1), 1L, 1L)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 예약_날짜가_오늘이고_현재_서버_시간_이전의_예약_시간이면_400을_반환한다() {
        createReservationPrerequisites(LocalTime.MIN);

        createReservationRequest(TODAY, 1L, 1L)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    @Test
    void 오늘_기준_30일을_초과한_날짜로_예약하면_400을_반환한다() {
        createReservationPrerequisites(LocalTime.of(15, 0));

        createReservationRequest(TODAY.plusDays(31), 1L, 1L)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(400);
    }

    private void createReservationPrerequisites(LocalTime startAt) {
        dataInitializer.createReservationTime(startAt);
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
    }

    private void createMemberReservation(String name, LocalDate date, Long timeId, Long themeId) {
        Member member = dataInitializer.createMember("member-" + name + "-" + date + "-" + timeId + "-" + themeId, "password", name);
        dataInitializer.createMemberReservation(member.getId(), member.getName(), date, timeId, themeId);
    }

    private void createCancelledReservation() {
        createReservationPrerequisites(LocalTime.of(10, 0));
        dataInitializer.createMemberReservation(loginMember.getId(), "고래", TODAY.plusDays(1), 1L, 1L);
        RestAssured.given()
                .when().post("/reservations/1/cancellations")
                .then().statusCode(200);
    }

    private RequestSpecification createReservationRequest(
            LocalDate date,
            Long timeId,
            Long themeId
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("date", date.toString());
        params.put("timeId", timeId);
        params.put("themeId", themeId);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params);
    }

    private Map<String, Object> loginParams(String loginId, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put("loginId", loginId);
        params.put("password", password);
        return params;
    }

}
