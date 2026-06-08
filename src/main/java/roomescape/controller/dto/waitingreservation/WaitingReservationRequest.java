package roomescape.controller.dto.waitingreservation;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.global.auth.LoginMember;
import roomescape.service.dto.waitingreservation.CreateWaitingReservationCommand;

public record WaitingReservationRequest(
        @NotNull(message = "매장은 필수입니다.")
        Long storeId,
        @NotNull(message = "예약 날짜는 필수입니다.")
        LocalDate date,
        @NotNull(message = "예약 시간은 필수입니다.")
        Long timeId,
        @NotNull(message = "테마는 필수입니다.")
        Long themeId
) {

    public CreateWaitingReservationCommand toCommand(LoginMember member) {
        return new CreateWaitingReservationCommand(
                storeId, member.id(), member.name(), date, timeId, themeId
        );
    }
}
