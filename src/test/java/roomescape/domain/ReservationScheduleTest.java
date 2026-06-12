package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.reservation.InvalidReservationException;

class ReservationScheduleTest {

    @Test
    void 같은_일정인지_판단한다() {
        ReservationSchedule schedule = ReservationSchedule.of(LocalDateTime.of(2026, 5, 20, 10, 0));
        ReservationSchedule sameSchedule = ReservationSchedule.of(LocalDateTime.of(2026, 5, 20, 10, 0));

        assertThat(schedule.hasSameSchedule(sameSchedule)).isTrue();
    }

    @Test
    void 지난_일정인지_판단한다() {
        ReservationSchedule schedule = ReservationSchedule.of(LocalDateTime.of(2026, 5, 20, 10, 0));

        assertThat(schedule.isExpired(LocalDateTime.of(2026, 5, 20, 10, 1))).isTrue();
    }

    @Test
    void 날짜가_없으면_생성할_수_없다() {
        assertThatThrownBy(() -> ReservationSchedule.of(null))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("예약 날짜, 시간은 필수입니다.");
    }
}
