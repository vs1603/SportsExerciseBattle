package at.fhtw.seb.presentation.handler;

import at.fhtw.seb.domain.HistoryEntry;
import at.fhtw.seb.domain.User;
import at.fhtw.seb.presentation.dtos.HistoryEntryRequestDto;
import at.fhtw.seb.presentation.dtos.HistoryEntryResponseDto;
import at.fhtw.seb.presentation.dtos.MessageDto;
import at.fhtw.seb.restserver.http.ContentType;
import at.fhtw.seb.restserver.http.HttpStatus;
import at.fhtw.seb.restserver.server.Response;
import at.fhtw.seb.service.HistoryService;
import at.fhtw.seb.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;


// Handles HTTP requests for the /history endpoint.
// GET  /history – returns current user's history
// POST /history – adds a new entry (may start/join tournament)

public class HistoryRequestHandler {

    private final HistoryService historyService;
    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    public HistoryRequestHandler(HistoryService historyService, UserService userService) {
        this.historyService = historyService;
        this.userService = userService;
    }

    // GET /history
    public Response handleGetHistory(String token) throws Exception {
        Response auth = checkAuth(token);
        if (auth != null) return auth;

        String username = userService.resolveToken(token);
        User user = userService.getByUsername(username);
        List<HistoryEntry> entries = historyService.getHistoryForUser(user.getId());

        List<HistoryEntryResponseDto> dtos = entries.stream()
                .map(e -> new HistoryEntryResponseDto(
                        e.getId(), e.getName(), e.getCount(), e.getDuration(),
                        e.getRecordedAt() != null ? e.getRecordedAt().toString() : null,
                        e.getTournamentId()))
                .collect(Collectors.toList());

        return new Response(HttpStatus.OK, ContentType.JSON, mapper.writeValueAsString(dtos));
    }

    // POST /history
    public Response handleAddEntry(String token, String body) throws Exception {
        Response auth = checkAuth(token);
        if (auth != null) return auth;

        String username = userService.resolveToken(token);
        User user = userService.getByUsername(username);

        HistoryEntryRequestDto dto = mapper.readValue(body, HistoryEntryRequestDto.class);
        try {
            HistoryEntry saved = historyService.addEntry(user.getId(), dto.getName(), dto.getCount(), dto.getDuration());

            HistoryEntryResponseDto response = new HistoryEntryResponseDto(
                    saved.getId(), saved.getName(), saved.getCount(), saved.getDuration(),
                    saved.getRecordedAt() != null ? saved.getRecordedAt().toString() : null,
                    saved.getTournamentId());

            return new Response(HttpStatus.CREATED, ContentType.JSON, mapper.writeValueAsString(response));
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // ---- Helpers ----

    private Response checkAuth(String token) throws Exception {
        if (!userService.isTokenValid(token)) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        return null;
    }

    private Response errorResponse(HttpStatus status, String message) throws Exception {
        String json = mapper.writeValueAsString(new MessageDto(message));
        return new Response(status, ContentType.JSON, json);
    }
}