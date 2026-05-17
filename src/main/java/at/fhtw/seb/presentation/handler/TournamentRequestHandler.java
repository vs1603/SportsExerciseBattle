package at.fhtw.seb.presentation.handler;

import at.fhtw.seb.domain.Tournament;
import at.fhtw.seb.presentation.dtos.MessageDto;
import at.fhtw.seb.presentation.dtos.TournamentResponseDto;
import at.fhtw.seb.restserver.http.ContentType;
import at.fhtw.seb.restserver.http.HttpStatus;
import at.fhtw.seb.restserver.server.Response;
import at.fhtw.seb.service.TournamentService;
import at.fhtw.seb.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

//Handles HTTP requests for the /tournament endpoint.
//GET /tournament  – returns current or latest tournament info

public class TournamentRequestHandler {

    private final TournamentService tournamentService;
    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    public TournamentRequestHandler(TournamentService tournamentService, UserService userService) {
        this.tournamentService = tournamentService;
        this.userService = userService;
    }

    // GET /tournament
    public Response handleGetTournament(String token) throws Exception {
        if (!userService.isTokenValid(token)) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }

        List<Tournament> all = tournamentService.getAll();
        if (all.isEmpty()) {
            String json = mapper.writeValueAsString(new MessageDto("No tournaments yet."));
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        }

        // Return all tournaments, newest first
        List<TournamentResponseDto> dtos = all.stream()
                .map(t -> new TournamentResponseDto(
                        t.getId(),
                        t.getStartTime().toString(),
                        t.getEndTime().toString(),
                        t.getState().name()))
                .collect(Collectors.toList());

        return new Response(HttpStatus.OK, ContentType.JSON, mapper.writeValueAsString(dtos));
    }

    // ---- Helper ----

    private Response errorResponse(HttpStatus status, String message) throws Exception {
        String json = mapper.writeValueAsString(new MessageDto(message));
        return new Response(status, ContentType.JSON, json);
    }
}