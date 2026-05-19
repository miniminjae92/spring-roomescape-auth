package roomescape.api;

import static org.hamcrest.Matchers.is;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.util.ApiTestSupport;
import roomescape.util.TestDataInitializer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AdminReservationApiTest extends ApiTestSupport {

    @Autowired
    private TestDataInitializer dataInitializer;

    @Test
    void 전체_예약_목록을_조회한다() {
        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(0));
    }

    @Test
    void 예약을_하드_삭제한다() {
        dataInitializer.createReservationTime(LocalTime.now());
        dataInitializer.createTheme("귀신의집", "무서워요", "/images/themes/reservation.webp");
        createMemberReservation("고래", LocalDate.now().plusDays(1), 1L, 1L);

        RestAssured.given().log().all()
                .when().delete("/admin/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
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
        RestAssured.given().log().all()
                .queryParam("page", page)
                .queryParam("size", size)
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(statusCode);
    }

    private void createMemberReservation(String name, LocalDate date, Long timeId, Long themeId) {
        Member member = dataInitializer.createMember("member-" + name + "-" + date + "-" + timeId + "-" + themeId, "password", name);
        dataInitializer.createMemberReservation(member.getId(), member.getName(), date, timeId, themeId);
    }
}
