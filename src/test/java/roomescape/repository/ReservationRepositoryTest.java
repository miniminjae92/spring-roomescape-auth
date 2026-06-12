package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.util.TestDataInitializer;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TestDataInitializer dataInitializer;

    private Theme theme;
    private Member whale;
    private Member shark;
    private ReservationTime ten;
    private ReservationTime eleven;
    private ReservationTime noon;

    @BeforeEach
    void setUp() {
        theme = dataInitializer.createTheme("테마", "설명", "/images/themes/theme.webp");
        whale = dataInitializer.createMember("whale", "password", "고래");
        shark = dataInitializer.createMember("shark", "password", "상어");
        ten = dataInitializer.createReservationTime(LocalTime.of(10, 0));
        eleven = dataInitializer.createReservationTime(LocalTime.of(11, 0));
        noon = dataInitializer.createReservationTime(LocalTime.of(12, 0));
    }

    @Test
    void 예약_목록을_페이징_조회한다() {
        createMemberReservation("사용자일", ten);
        Reservation second = createMemberReservation("사용자이", eleven);
        Reservation third = createMemberReservation("사용자삼", noon);

        List<ReservationData> reservations = reservationRepository.findAll(2, 1);

        assertThat(reservations).extracting(reservationData -> reservationData.reservation().getId())
                .containsExactly(second.getId(), third.getId());
    }

    @Test
    void 회원으로_예약_이력을_페이징_조회한다() {
        Reservation first = createMemberReservation(whale, ten);
        createMemberReservation(shark, eleven);
        Reservation second = createMemberReservation(whale, noon);

        List<ReservationData> reservations = reservationRepository.findAllByMemberId(whale.getId(), 1, 1);

        assertThat(reservations).extracting(reservationData -> reservationData.reservation().getId())
                .containsExactly(second.getId());
    }

    @Test
    void 회원에_해당하는_예약이_없으면_빈_목록을_반환한다() {
        createMemberReservation(whale, ten);

        List<ReservationData> reservations = reservationRepository.findAllByMemberId(shark.getId(), 20, 0);

        assertThat(reservations).isEmpty();
    }

    private Reservation createMemberReservation(String name, ReservationTime time) {
        Member member = dataInitializer.createMember("member-" + name, "password", name);
        return createMemberReservation(member, time);
    }

    private Reservation createMemberReservation(Member member, ReservationTime time) {
        return dataInitializer.createMemberReservation(
                member.getId(),
                member.getName(),
                LocalDate.of(2026, 5, 20),
                time.getId(),
                theme.getId()
        );
    }
}
