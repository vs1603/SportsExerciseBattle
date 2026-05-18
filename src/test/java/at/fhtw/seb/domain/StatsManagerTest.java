package at.fhtw.seb.domain;

import at.fhtw.seb.persistence.HistoryRepository;
import at.fhtw.seb.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsManagerTest {

    private UserRepository userRepo;
    private HistoryRepository historyRepo;
    private StatsManager statsManager;

    @BeforeEach
    void setUp() {
        userRepo     = mock(UserRepository.class);
        historyRepo  = mock(HistoryRepository.class);
        statsManager = new StatsManager(userRepo, historyRepo);
    }

    @Test
    void getStatsForUser_returnsCorrectValues() {
        User alice = new User(1, "alice", "hash", 120, null, null, null);
        when(userRepo.findByUsername("alice")).thenReturn(alice);
        when(historyRepo.findByUserId(1)).thenReturn(List.of(
                entry(1, 80),
                entry(1, 70)
        ));

        UserStats stats = statsManager.getStatsForUser("alice");

        assertEquals("alice", stats.getUsername());
        assertEquals(120, stats.getElo());
        assertEquals(150, stats.getTotalPushUps());
        assertEquals(2, stats.getTotalEntries());
    }

    @Test
    void getStatsForUser_noEntries_returnsZeros() {
        User alice = new User(1, "alice", "hash", 100, null, null, null);
        when(userRepo.findByUsername("alice")).thenReturn(alice);
        when(historyRepo.findByUserId(1)).thenReturn(List.of());

        UserStats stats = statsManager.getStatsForUser("alice");

        assertEquals(0, stats.getTotalPushUps());
        assertEquals(0, stats.getTotalEntries());
    }

    @Test
    void getStatsForUser_throwsWhenUserNotFound() {
        when(userRepo.findByUsername("idontexist")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> statsManager.getStatsForUser("idontexist"));
    }

    @Test
    void getScoreboard_sortedByEloDescending() {
        User alice = new User(1, "alice", "h", 90,  null, null, null);
        User bob   = new User(2, "bob",   "h", 110, null, null, null);
        when(userRepo.findAll()).thenReturn(List.of(alice, bob));
        when(historyRepo.findByUserId(1)).thenReturn(List.of());
        when(historyRepo.findByUserId(2)).thenReturn(List.of());

        List<ScoreboardEntry> board = statsManager.getScoreboard();

        assertEquals("bob",   board.get(0).getUsername());
        assertEquals("alice", board.get(1).getUsername());
    }

    @Test
    void getScoreboard_sameElo_sortedByTotalPushUpsDescending() {
        User alice = new User(1, "alice", "h", 100, null, null, null);
        User bob   = new User(2, "bob",   "h", 100, null, null, null);
        when(userRepo.findAll()).thenReturn(List.of(alice, bob));
        when(historyRepo.findByUserId(1)).thenReturn(List.of(entry(1, 30)));
        when(historyRepo.findByUserId(2)).thenReturn(List.of(entry(2, 80)));

        List<ScoreboardEntry> board = statsManager.getScoreboard();

        assertEquals("bob",   board.get(0).getUsername());
        assertEquals("alice", board.get(1).getUsername());
    }

    // ---- Helper ----

    private HistoryEntry entry(int userId, int count) {
        HistoryEntry e = new HistoryEntry();
        e.setUserId(userId);
        e.setCount(count);
        e.setName("PushUps");
        e.setDuration(60);
        e.setRecordedAt(LocalDateTime.now());
        return e;
    }
}