package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.reservation.InvalidReservationException;

class ReservationSlotTest {

    private static final Theme THEME = Theme.from(
            1L, "테마", "설명", "/images/themes/theme.webp"
    );

    @Test
    void 같은_일정인지_판단한다() {
        ReservationTime time = ReservationTime.from(1L, LocalTime.of(10, 0));
        ReservationSlot slot = ReservationSlot.of(1L, LocalDate.of(2026, 5, 20), time, THEME);

        assertThat(slot.isSameSchedule(LocalDate.of(2026, 5, 20), time)).isTrue();
    }

    @Test
    void 시작_10분_전부터_사용자_작업이_마감된다() {
        ReservationTime time = ReservationTime.from(1L, LocalTime.of(10, 0));
        ReservationSlot slot = ReservationSlot.of(1L, LocalDate.of(2026, 5, 20), time, THEME);
        Clock clock = Clock.fixed(
                Instant.parse("2026-05-20T00:50:00Z"),
                ZoneId.of("Asia/Seoul")
        );

        assertThat(slot.isClosedForUser(clock)).isTrue();
    }

    @Test
    void 날짜가_없으면_생성할_수_없다() {
        ReservationTime time = ReservationTime.from(1L, LocalTime.of(10, 0));

        assertThatThrownBy(() -> ReservationSlot.of(1L, null, time, THEME))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("예약 날짜, 시간은 필수입니다.");
    }
}
