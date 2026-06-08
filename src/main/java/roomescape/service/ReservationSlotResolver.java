package roomescape.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.global.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.global.exception.store.StoreNotFoundException;
import roomescape.global.exception.theme.ThemeNotFoundException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.StoreRepository;
import roomescape.repository.ThemeRepository;

@Component
@RequiredArgsConstructor
public class ReservationSlotResolver {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final StoreRepository storeRepository;

    public ReservationSlot resolve(Long storeId, LocalDate date, Long timeId, Long themeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new StoreNotFoundException("선택한 매장이 존재하지 않습니다.");
        }
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("선택한 예약 시간이 존재하지 않습니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("선택한 테마가 존재하지 않습니다."));
        return ReservationSlot.of(storeId, date, time, theme);
    }
}
