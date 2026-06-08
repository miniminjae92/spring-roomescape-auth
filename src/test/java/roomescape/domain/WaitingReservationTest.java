package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.waitingreservation.WaitingReservationStateException;

class WaitingReservationTest {

    @Test
    void 대기를_취소하면_CANCELLED_상태가_된다() {
        WaitingReservation waiting = waiting();

        WaitingReservation cancelled = waiting.cancel();

        assertThat(cancelled.getStatus()).isEqualTo(WaitingReservationStatus.CANCELLED);
    }

    @Test
    void 대기를_승격하면_생성된_예약을_기록한다() {
        WaitingReservation promoted = waiting().promote(10L);

        assertThat(promoted.getStatus()).isEqualTo(WaitingReservationStatus.PROMOTED);
        assertThat(promoted.getPromotedReservationId()).isEqualTo(10L);
    }

    @Test
    void 처리된_대기는_다시_처리할_수_없다() {
        WaitingReservation cancelled = waiting().cancel();

        assertThatThrownBy(cancelled::cancel)
                .isInstanceOf(WaitingReservationStateException.class);
    }

    private WaitingReservation waiting() {
        ReservationSlot slot = ReservationSlot.of(
                1L,
                LocalDate.of(2026, 6, 10),
                ReservationTime.from(1L, LocalTime.of(10, 0)),
                Theme.from(1L, "테마", "설명", "/images/themes/theme.webp")
        );
        return WaitingReservation.createNew(1L, "고래", slot, LocalDateTime.of(2026, 6, 8, 10, 0));
    }
}
