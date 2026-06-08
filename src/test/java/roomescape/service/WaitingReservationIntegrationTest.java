package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.domain.WaitingReservationStatus;
import roomescape.global.exception.waitingreservation.DuplicateWaitingReservationException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingReservationRepository;
import roomescape.service.dto.reservation.CancelReservationCommand;
import roomescape.service.dto.waitingreservation.CreateWaitingReservationCommand;
import roomescape.util.TestDataInitializer;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WaitingReservationIntegrationTest {

    @Autowired
    private TestDataInitializer dataInitializer;

    @Autowired
    private WaitingReservationService waitingReservationService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Member owner;
    private Member firstWaiter;
    private Member secondWaiter;
    private Reservation reservation;
    private LocalDate date;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        Store store = dataInitializer.createStore("강남점");
        owner = dataInitializer.createMember("owner", "password", "예약자");
        firstWaiter = dataInitializer.createMember("first", "password", "대기자일");
        secondWaiter = dataInitializer.createMember("second", "password", "대기자이");
        time = dataInitializer.createReservationTime(LocalTime.of(10, 0));
        theme = dataInitializer.createTheme("테마", "설명", "/images/themes/theme.webp");
        date = LocalDate.now().plusDays(2);
        reservation = dataInitializer.createMemberReservation(
                store.getId(), owner.getId(), owner.getName(), date, time.getId(), theme.getId()
        );
    }

    @Test
    void 예약을_취소하면_가장_먼저_신청한_대기가_같은_트랜잭션에서_승격된다() {
        Long firstWaitingId = waitingReservationService.create(command(firstWaiter)).id();
        Long secondWaitingId = waitingReservationService.create(command(secondWaiter)).id();

        reservationService.cancelReservation(new CancelReservationCommand(reservation.getId(), owner.getId()));

        var promoted = waitingReservationRepository.findById(firstWaitingId).orElseThrow();
        var remaining = waitingReservationRepository.findById(secondWaitingId).orElseThrow();
        Reservation promotedReservation = reservationRepository.findById(promoted.getPromotedReservationId())
                .orElseThrow();

        assertThat(promoted.getStatus()).isEqualTo(WaitingReservationStatus.PROMOTED);
        assertThat(promotedReservation.getMemberId()).isEqualTo(firstWaiter.getId());
        assertThat(remaining.getStatus()).isEqualTo(WaitingReservationStatus.WAITING);
    }

    @Test
    void 같은_회원의_동시_대기_신청은_하나만_성공한다() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<String> task = () -> {
            start.await();
            try {
                waitingReservationService.create(command(firstWaiter));
                return "SUCCESS";
            } catch (DuplicateWaitingReservationException e) {
                return "DUPLICATE";
            }
        };

        Future<String> first = executor.submit(task);
        Future<String> second = executor.submit(task);
        start.countDown();
        List<String> results = List.of(first.get(), second.get());
        executor.shutdown();

        assertThat(results).containsExactlyInAnyOrder("SUCCESS", "DUPLICATE");
    }

    private CreateWaitingReservationCommand command(Member member) {
        return new CreateWaitingReservationCommand(
                reservation.getStoreId(),
                member.getId(),
                member.getName(),
                date,
                time.getId(),
                theme.getId()
        );
    }
}
