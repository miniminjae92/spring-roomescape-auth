package roomescape.controller.dto.reservation;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.global.auth.LoginMember;
import roomescape.service.dto.reservation.CreateReservationCommand;

public record ReservationRequest(
        @NotNull(message = "예약 날짜는 필수입니다.")
        LocalDate date,

        @NotNull(message = "예약 시간은 필수입니다.")
        Long timeId,

        @NotNull(message = "테마는 필수입니다.")
        Long themeId
) {

    public CreateReservationCommand toCommand(LoginMember loginMember) {
        return new CreateReservationCommand(
                loginMember.id(),
                loginMember.name(),
                date,
                timeId,
                themeId
        );
    }
}
