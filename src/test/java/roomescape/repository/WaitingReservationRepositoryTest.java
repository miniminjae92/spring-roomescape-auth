package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingReservation;
import roomescape.repository.dto.WaitingReservationWithRank;

@JdbcTest
@Import(WaitingReservationRepository.class)
class WaitingReservationRepositoryTest {

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ReservationSlot slot;
    private Long firstMemberId;
    private Long secondMemberId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("""
                INSERT INTO member (login_id, password, name, role)
                VALUES ('member1', 'password', '고래', 'USER'),
                       ('member2', 'password', '상어', 'USER')
                """);
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", LocalTime.of(10, 0));
        jdbcTemplate.update("""
                INSERT INTO theme (name, description, image_path)
                VALUES ('테마', '설명', '/images/themes/theme.webp')
                """);
        firstMemberId = jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE login_id = 'member1'",
                Long.class
        );
        secondMemberId = jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE login_id = 'member2'",
                Long.class
        );
        Long timeId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = '10:00:00'",
                Long.class
        );
        Long themeId = jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = '테마'",
                Long.class
        );
        slot = ReservationSlot.of(
                1L,
                LocalDate.of(2026, 6, 10),
                ReservationTime.from(timeId, LocalTime.of(10, 0)),
                Theme.from(themeId, "테마", "설명", "/images/themes/theme.webp")
        );
    }

    @Test
    void 같은_회원의_활성_대기는_슬롯별로_하나만_저장된다() {
        waitingReservationRepository.save(waiting(firstMemberId, LocalDateTime.of(2026, 6, 8, 10, 0)));

        assertThatThrownBy(() ->
                waitingReservationRepository.save(waiting(firstMemberId, LocalDateTime.of(2026, 6, 8, 10, 1)))
        ).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 대기_순번과_가장_오래된_대기를_조회한다() {
        WaitingReservation first = waitingReservationRepository.save(
                waiting(firstMemberId, LocalDateTime.of(2026, 6, 8, 10, 0))
        );
        WaitingReservation second = waitingReservationRepository.save(
                waiting(secondMemberId, LocalDateTime.of(2026, 6, 8, 10, 1))
        );

        WaitingReservationWithRank ranked = waitingReservationRepository
                .findActiveByMemberId(secondMemberId, 20, 0)
                .getFirst();
        WaitingReservation oldest = waitingReservationRepository
                .findOldestActiveBySlotForUpdate(slot)
                .orElseThrow();

        assertThat(ranked.waitingReservation().getId()).isEqualTo(second.getId());
        assertThat(ranked.rank()).isEqualTo(2);
        assertThat(oldest.getId()).isEqualTo(first.getId());
    }

    private WaitingReservation waiting(Long memberId, LocalDateTime createdAt) {
        String name = memberId.equals(firstMemberId) ? "고래" : "상어";
        return WaitingReservation.createNew(memberId, name, slot, createdAt);
    }
}
