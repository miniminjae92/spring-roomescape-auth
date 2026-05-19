package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.reservation.InvalidReservationException;

class ReservationScheduleTest {

    @Test
    void 같은_일정인지_판단한다() {
        ReservationTime time = ReservationTime.createNew(LocalTime.of(10, 0));
        ReservationSchedule schedule = ReservationSchedule.of(LocalDate.of(2026, 5, 20), time);

        assertThat(schedule.hasSameSchedule(LocalDate.of(2026, 5, 20), time)).isTrue();
    }

    @Test
    void 지난_일정인지_판단한다() {
        ReservationTime time = ReservationTime.createNew(LocalTime.of(10, 0));
        ReservationSchedule schedule = ReservationSchedule.of(LocalDate.of(2026, 5, 20), time);

        assertThat(schedule.isExpired(LocalDate.of(2026, 5, 20), LocalTime.of(10, 1))).isTrue();
    }

    @Test
    void 날짜가_없으면_생성할_수_없다() {
        ReservationTime time = ReservationTime.createNew(LocalTime.of(10, 0));

        assertThatThrownBy(() -> ReservationSchedule.of(null, time))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("예약 날짜, 시간은 필수입니다.");
    }
}
