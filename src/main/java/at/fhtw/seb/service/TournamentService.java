package at.fhtw.seb.service;

import at.fhtw.seb.domain.Tournament;
import at.fhtw.seb.domain.TournamentManager;

import java.util.List;

public class TournamentService {

    private final TournamentManager tournamentManager;

    public TournamentService(TournamentManager tournamentManager) {
        this.tournamentManager = tournamentManager;
    }

    public List<Tournament> getAll() {
        return tournamentManager.getAll();
    }

    public Tournament getCurrentOrLatest() {
        return tournamentManager.getCurrentOrLatest();
    }
}