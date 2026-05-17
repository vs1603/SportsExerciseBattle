package at.fhtw.seb.presentation.handler;

import at.fhtw.seb.domain.ScoreboardEntry;
import at.fhtw.seb.domain.UserStats;

import at.fhtw.seb.presentation.dtos.MessageDto;
import at.fhtw.seb.presentation.dtos.ScoreboardEntryDto;
import at.fhtw.seb.presentation.dtos.StatsResponseDto;
import at.fhtw.seb.restserver.http.ContentType;
import at.fhtw.seb.restserver.http.HttpStatus;
import at.fhtw.seb.restserver.server.Response;
import at.fhtw.seb.service.StatsService;
import at.fhtw.seb.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

// Handles HTTP requests for /stats and /score endpoints.
// GET /stats  – individual user stats (ELO + total push-ups)
// GET /score  – global scoreboard

public class StatsRequestHandler {

    private final StatsService statsService;
    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    public StatsRequestHandler(StatsService statsService, UserService userService) {
        this.statsService = statsService;
        this.userService = userService;
    }

    // GET /stats
    public Response handleStats(String token) throws Exception {
        if (!userService.isTokenValid(token)) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        String username = userService.resolveToken(token);
        UserStats stats = statsService.getStatsForUser(username);
        StatsResponseDto dto = new StatsResponseDto(
                stats.getUsername(), stats.getElo(), stats.getTotalPushUps(), stats.getTotalEntries());
        return new Response(HttpStatus.OK, ContentType.JSON, mapper.writeValueAsString(dto));
    }

    // GET /score
    public Response handleScore(String token) throws Exception {
        if (!userService.isTokenValid(token)) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        List<ScoreboardEntry> board = statsService.getScoreboard();
        List<ScoreboardEntryDto> dtos = board.stream()
                .map(e -> new ScoreboardEntryDto(
                        e.getUsername(), e.getElo(), e.getTotalPushUps(), e.getTotalEntries()))
                .collect(Collectors.toList());
        return new Response(HttpStatus.OK, ContentType.JSON, mapper.writeValueAsString(dtos));
    }

    // ---- Helper ----

    private Response errorResponse(HttpStatus status, String message) throws Exception {
        String json = mapper.writeValueAsString(new MessageDto(message));
        return new Response(status, ContentType.JSON, json);
    }
}