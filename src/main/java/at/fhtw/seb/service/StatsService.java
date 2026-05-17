package at.fhtw.seb.service;

import at.fhtw.seb.domain.ScoreboardEntry;
import at.fhtw.seb.domain.StatsManager;
import at.fhtw.seb.domain.UserStats;

import java.util.List;

public class StatsService {

    private final StatsManager statsManager;

    public StatsService(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    public UserStats getStatsForUser(String username) {
        return statsManager.getStatsForUser(username);
    }

    public List<ScoreboardEntry> getScoreboard() {
        return statsManager.getScoreboard();
    }
}