package at.fhtw.seb.service;

import at.fhtw.seb.domain.HistoryEntry;
import at.fhtw.seb.domain.TournamentManager;
import at.fhtw.seb.persistence.HistoryRepository;

import java.util.List;

public class HistoryService {

    private final TournamentManager tournamentManager;
    private final HistoryRepository historyRepo;

    public HistoryService(TournamentManager tournamentManager,
                          HistoryRepository historyRepo) {
        this.tournamentManager = tournamentManager;
        this.historyRepo = historyRepo;
    }

    public HistoryEntry addEntry(int userId, int count, int duration) {
        return tournamentManager.addEntry(userId, count, duration);
    }

    public List<HistoryEntry> getHistoryForUser(int userId) {
        return historyRepo.findByUserId(userId);
    }
}