package at.fhtw.seb.domain;

import at.fhtw.seb.persistence.HistoryRepository;
import at.fhtw.seb.persistence.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StatsManager {

    private final UserRepository userRepo;
    private final HistoryRepository historyRepo;

    public StatsManager(UserRepository userRepo, HistoryRepository historyRepo) {
        this.userRepo = userRepo;
        this.historyRepo = historyRepo;
    }

    public UserStats getStatsForUser(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null)
            throw new IllegalArgumentException("User not found: " + username);

        List<HistoryEntry> entries = historyRepo.findByUserId(user.getId());
        int totalPushUps = entries.stream().mapToInt(HistoryEntry::getCount).sum();
        int totalEntries = entries.size();

        return new UserStats(user.getUsername(), user.getElo(), totalPushUps, totalEntries);
    }

    public List<ScoreboardEntry> getScoreboard() {
        return userRepo.findAll().stream()
                .map(u -> {
                    List<HistoryEntry> entries = historyRepo.findByUserId(u.getId());
                    int totalPushUps = entries.stream().mapToInt(HistoryEntry::getCount).sum();
                    int totalEntries = entries.size();
                    return new ScoreboardEntry(u.getUsername(), u.getElo(), totalPushUps, totalEntries);
                })
                .sorted(Comparator.comparingInt(ScoreboardEntry::getElo).reversed()
                        .thenComparing(Comparator.comparingInt(ScoreboardEntry::getTotalPushUps).reversed()))
                .collect(Collectors.toList());
    }
}