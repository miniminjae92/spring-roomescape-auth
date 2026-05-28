package roomescape.util;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.global.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.global.exception.theme.ThemeNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.StoreManagerRepository;
import roomescape.repository.StoreRepository;
import roomescape.repository.ThemeRepository;

@Component
public class TestDataInitializer {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final StoreRepository storeRepository;
    private final StoreManagerRepository storeManagerRepository;

    public TestDataInitializer(ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                               ReservationRepository reservationRepository, MemberRepository memberRepository,
                               StoreRepository storeRepository, StoreManagerRepository storeManagerRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.storeRepository = storeRepository;
        this.storeManagerRepository = storeManagerRepository;
    }

    public ReservationTime createReservationTime(LocalTime localTime) {
        return reservationTimeRepository.save(ReservationTime.createNew(localTime.withSecond(0).withNano(0)));
    }

    public Theme createTheme(String name, String description, String imagePath) {
        return themeRepository.save(Theme.createNew(name, description, imagePath));
    }

    public Member createMember(String loginId, String password, String name) {
        return memberRepository.save(Member.createUser(loginId, password, name));
    }

    public Member createManager(String loginId, String password, String name) {
        return memberRepository.save(Member.createManager(loginId, password, name));
    }

    public Store createStore(String name) {
        return storeRepository.save(Store.createNew(name));
    }

    public void createStoreManager(Long storeId, Long memberId) {
        storeManagerRepository.save(storeId, memberId);
    }

    public Reservation createMemberReservation(Long memberId, String name, LocalDate date, Long timeId, Long themeId) {
        return createMemberReservation(1L, memberId, name, date, timeId, themeId);
    }

    public Reservation createMemberReservation(Long storeId, Long memberId, String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime reservationTime = getReservationTime(timeId);
        Theme theme = getTheme(themeId);
        return reservationRepository.save(Reservation.createNew(storeId, memberId, name, date, reservationTime, theme));
    }

    private ReservationTime getReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("선택한 예약 시간이 존재하지 않습니다."));
    }

    private Theme getTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("선택한 테마가 존재하지 않습니다."));
    }
}
