package roomescape.util;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Component;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.repository.MemberRepository;
import roomescape.global.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.global.exception.theme.ThemeNotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Component
public class TestDataInitializer {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public TestDataInitializer(ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                               ReservationRepository reservationRepository, MemberRepository memberRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationTime createReservationTime(LocalTime localTime) {
        return reservationTimeRepository.save(ReservationTime.createNew(localTime.withSecond(0).withNano(0)));
    }

    public Theme createTheme(String name, String description, String imagePath) {
        return themeRepository.save(Theme.createNew(name, description, imagePath));
    }

    public Member createMember(String loginId, String password, String name) {
        return memberRepository.save(Member.createNew(loginId, password, name));
    }

    public Reservation createReservation(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("선택한 예약 시간이 존재하지 않습니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("선택한 테마가 존재하지 않습니다."));
        return reservationRepository.save(Reservation.createNew(name, date, reservationTime, theme));
    }

    public Reservation createMemberReservation(Long memberId, String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("선택한 예약 시간이 존재하지 않습니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("선택한 테마가 존재하지 않습니다."));
        return reservationRepository.save(Reservation.createNew(memberId, name, date, reservationTime, theme));
    }
}
