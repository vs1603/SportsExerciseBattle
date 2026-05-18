package at.fhtw.seb.domain;

import at.fhtw.seb.persistence.HistoryRepository;
import at.fhtw.seb.persistence.TournamentRepository;
import at.fhtw.seb.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

class TournamentManagerEloTest {

    private TournamentRepository tournamentRepo;
    private HistoryRepository historyRepo;
    private UserRepository userRepo;
    private TournamentManager manager;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        tournamentRepo = mock(TournamentRepository.class);
        historyRepo    = mock(HistoryRepository.class);
        userRepo       = mock(UserRepository.class);
        manager = new TournamentManager(tournamentRepo, historyRepo, userRepo);
        tournament = new Tournament(1,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().minusMinutes(3),
                TournamentState.ENDED);
    }

    @Test
    void singleWinner_getsPlus2_otherGetsMinus1() {
        // user 1: 50 reps, user 2: 30 reps
        when(historyRepo.findByTournamentId(1)).thenReturn(List.of(
                entry(1, 50), entry(2, 30)
        ));

        manager.distributeElo(tournament);

        verify(userRepo).adjustElo(1, 2);
        verify(userRepo).adjustElo(2, -1);
    }

    @Test
    void draw_bothGetPlus1() {
        when(historyRepo.findByTournamentId(1)).thenReturn(List.of(
                entry(1, 40), entry(2, 40)
        ));

        manager.distributeElo(tournament);

        verify(userRepo).adjustElo(1, 1);
        verify(userRepo).adjustElo(2, 1);
    }

    @Test
    void threeWayDraw_allGetPlus1() {
        when(historyRepo.findByTournamentId(1)).thenReturn(List.of(
                entry(1, 20), entry(2, 20), entry(3, 20)
        ));

        manager.distributeElo(tournament);

        verify(userRepo).adjustElo(1, 1);
        verify(userRepo).adjustElo(2, 1);
        verify(userRepo).adjustElo(3, 1);
    }

    @Test
    void singleParticipant_noEloChange() {
        when(historyRepo.findByTournamentId(1)).thenReturn(List.of(
                entry(1, 50)
        ));

        manager.distributeElo(tournament);

        verifyNoInteractions(userRepo);
    }

    @Test
    void multipleEntriesPerUser_sumsAreCombined() {
        when(historyRepo.findByTournamentId(1)).thenReturn(List.of(
                entry(1, 30), entry(1, 30), entry(2, 50)
        ));

        manager.distributeElo(tournament);

        verify(userRepo).adjustElo(1, 2);
        verify(userRepo).adjustElo(2, -1);
    }

    @Test
    void multipleLosersBothGetMinus1() {
        when(historyRepo.findByTournamentId(1)).thenReturn(List.of(
                entry(1, 100), entry(2, 10), entry(3, 5)
        ));

        manager.distributeElo(tournament);

        verify(userRepo).adjustElo(1, 2);
        verify(userRepo).adjustElo(2, -1);
        verify(userRepo).adjustElo(3, -1);
    }

    // ---- Helper ----

    private HistoryEntry entry(int userId, int count) {
        HistoryEntry e = new HistoryEntry();
        e.setUserId(userId);
        e.setCount(count);
        e.setTournamentId(1);
        e.setName("PushUps");
        e.setDuration(60);
        e.setRecordedAt(LocalDateTime.now());
        return e;
    }
}