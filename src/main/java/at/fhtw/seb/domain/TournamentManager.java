package at.fhtw.seb.domain;

import at.fhtw.seb.persistence.HistoryRepository;
import at.fhtw.seb.persistence.TournamentRepository;
import at.fhtw.seb.persistence.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TournamentManager {

    public static final long DURATION_MINUTES = 2;

    private final TournamentRepository tournamentRepo;
    private final HistoryRepository historyRepo;
    private final UserRepository userRepo;

    public TournamentManager(TournamentRepository tournamentRepo,
                             HistoryRepository historyRepo,
                             UserRepository userRepo) {
        this.tournamentRepo = tournamentRepo;
        this.historyRepo = historyRepo;
        this.userRepo = userRepo;
    }

    public HistoryEntry addEntry(int userId, int count, int duration) {
        LocalDateTime now = LocalDateTime.now();

        // Close any expired running tournament before deciding
        closeExpiredTournaments(now);

        Tournament tournament = tournamentRepo.findRunning();

        if (tournament == null)
            tournament = startNewTournament(now);

        HistoryEntry entry = new HistoryEntry(
                null, userId, tournament.getId(), count, duration, now);
        HistoryEntry saved = historyRepo.save(entry);

        System.out.printf("[Tournament #%d] User %d did x%d push-ups in %ds\n",
                tournament.getId(), userId, count, duration);

        return saved;
    }

    public List<Tournament> getAll() {
        closeExpiredTournaments(LocalDateTime.now());
        return tournamentRepo.findAll();
    }

    public Tournament getCurrentOrLatest() {
        closeExpiredTournaments(LocalDateTime.now());
        Tournament running = tournamentRepo.findRunning();
        if (running != null) return running;
        List<Tournament> all = tournamentRepo.findAll();
        return all.isEmpty() ? null : all.get(0);
    }

    // ---- Internal helpers ----

    private Tournament startNewTournament(LocalDateTime now) {
        Tournament t = new Tournament(
                null,
                now,
                now.plusMinutes(DURATION_MINUTES),
                TournamentState.RUNNING);
        Tournament saved = tournamentRepo.save(t);
        System.out.println("[TournamentManager] Started tournament #" + saved.getId()
                + " ends at " + saved.getEndTime());
        return saved;
    }

    // Closes all RUNNING tournaments whose end time has passed and distributes ELO.
    private void closeExpiredTournaments(LocalDateTime now) {
        Tournament running = tournamentRepo.findRunning();
        if (running != null && !now.isBefore(running.getEndTime())) {
            tournamentRepo.markEnded(running.getId());
            distributeElo(running);
            System.out.println("[TournamentManager] Closed tournament #" + running.getId());
        }
    }

    void distributeElo(Tournament tournament) {
        List<HistoryEntry> entries = historyRepo.findByTournamentId(tournament.getId());

        // sum counts per user
        Map<Integer, Integer> sumByUser = new HashMap<>();
        for (HistoryEntry e : entries) {
            if (sumByUser.containsKey(e.getUserId())) {
                sumByUser.put(e.getUserId(), sumByUser.get(e.getUserId()) + e.getCount());
            } else {
                sumByUser.put(e.getUserId(), e.getCount());
            }
        }

        if (sumByUser.size() < 2) {
            System.out.println("[ELO] Only " + sumByUser.size()
                    + " participant(s) in tournament #" + tournament.getId() + " – no ELO change.");
            return;
        }

        int maxCount = sumByUser.values().stream().mapToInt(Integer::intValue).max().orElse(0);

        Set<Integer> winners = sumByUser.entrySet().stream() // get all key-value pairs from the map
                .filter(e -> e.getValue() == maxCount)// keep only entries where value == maxCount
                .map(Map.Entry::getKey) // extract only the userId (key)
                .collect(Collectors.toSet()); // collect into a Set

        boolean isDraw = winners.size() > 1;
        int winnerDelta = isDraw ? 1 : 2;

        for (Map.Entry<Integer, Integer> entry : sumByUser.entrySet()) {
            int uid = entry.getKey();
            if (winners.contains(uid)) {
                userRepo.adjustElo(uid, winnerDelta);
                System.out.printf("[ELO] User %d: +%d (sum=%d, %s)\n",
                        uid, winnerDelta, entry.getValue(), isDraw ? "draw" : "winner");
            } else {
                userRepo.adjustElo(uid, -1);
                System.out.printf("[ELO] User %d: -1 (sum=%d)\n", uid, entry.getValue());
            }
        }
    }
}