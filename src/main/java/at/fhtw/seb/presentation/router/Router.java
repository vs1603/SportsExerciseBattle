package at.fhtw.seb.presentation.router;

import at.fhtw.seb.presentation.handler.*;
import at.fhtw.seb.restserver.http.ContentType;
import at.fhtw.seb.restserver.http.HttpStatus;
import at.fhtw.seb.restserver.http.Method;
import at.fhtw.seb.restserver.server.Request;
import at.fhtw.seb.restserver.server.Response;

import java.util.List;

/*
 *   POST   /users                register
 *   GET    /users/{username}     get own profile
 *   PUT    /users/{username}     update own profile
 *   POST   /sessions             login
 *   DELETE /sessions             logout
 *   GET    /history              get own history
 *   POST   /history              add entry (may start/join tournament)
 *   GET    /stats                get own stats (ELO + push-up count)
 *   GET    /score                global scoreboard
 *   GET    /tournament           current/latest tournament info
 */
public class Router {

    private final UserRequestHandler userHandler;
    private final HistoryRequestHandler historyHandler;
    private final StatsRequestHandler statsHandler;
    private final TournamentRequestHandler tournamentHandler;

    public Router(UserRequestHandler userHandler,
                  HistoryRequestHandler historyHandler,
                  StatsRequestHandler statsHandler,
                  TournamentRequestHandler tournamentHandler) {
        this.userHandler = userHandler;
        this.historyHandler = historyHandler;
        this.statsHandler = statsHandler;
        this.tournamentHandler = tournamentHandler;
    }

    public Response route(Request request, String httpMethod, String body) throws Exception {
        List<String> parts = request.getPathParts();
        String token = request.getBearerToken();

        if (parts.isEmpty()) {
            return notFound();
        }

        String first = parts.get(0);

        // ---- /users ----
        if (first.equals("users")) {
            if (parts.size() == 1 && httpMethod.equals(Method.POST.name())) {
                return userHandler.handleRegister(body);
            }
            if (parts.size() == 2) {
                String username = parts.get(1);
                if (httpMethod.equals(Method.GET.name())) {
                    return userHandler.handleGetUser(username, token);
                }
                if (httpMethod.equals(Method.PUT.name())) {
                    return userHandler.handleUpdateUser(username, token, body);
                }
            }
        }

        // ---- /sessions ----
        if (first.equals("sessions") && parts.size() == 1) {
            if (httpMethod.equals(Method.POST.name())) {
                return userHandler.handleLogin(body);
            }
            if (httpMethod.equals(Method.DELETE.name())) {
                return userHandler.handleLogout(token);
            }
        }

        // ---- /history ----
        if (first.equals("history") && parts.size() == 1) {
            if (httpMethod.equals(Method.GET.name())) {
                return historyHandler.handleGetHistory(token);
            }
            if (httpMethod.equals(Method.POST.name())) {
                return historyHandler.handleAddEntry(token, body);
            }
        }

        // ---- /stats ----
        if (first.equals("stats") && parts.size() == 1
                && httpMethod.equals(Method.GET.name())) {
            return statsHandler.handleStats(token);
        }

        // ---- /score ----
        if (first.equals("score") && parts.size() == 1
                && httpMethod.equals(Method.GET.name())) {
            return statsHandler.handleScore(token);
        }

        // ---- /tournament ----
        if (first.equals("tournament") && parts.size() == 1
                && httpMethod.equals(Method.GET.name())) {
            return tournamentHandler.handleGetTournament(token);
        }

        return notFound();
    }

    private Response notFound() {
        return new Response(HttpStatus.NOT_FOUND, ContentType.JSON,
                "{\"message\":\"Route not found.\"}");
    }
}