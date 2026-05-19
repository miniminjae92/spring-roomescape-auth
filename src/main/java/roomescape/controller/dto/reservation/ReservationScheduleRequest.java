package roomescape.controller.dto.reservation;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.global.auth.LoginMember;
import roomescape.service.dto.reservation.ChangeReservationScheduleCommand;

public record ReservationScheduleRequest(
        @NotNull(message = "예약 날짜는 필수입니다.")
        LocalDate date,

        @NotNull(message = "예약 시간은 필수입니다.")
        Long timeId
) {

    public ChangeReservationScheduleCommand toCommand(Long reservationId, LoginMember loginMember) {
        return new ChangeReservationScheduleCommand(reservationId, loginMember.id(), date, timeId);
    }
}
