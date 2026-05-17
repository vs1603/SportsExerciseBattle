package at.fhtw.seb.restserver.server;

import at.fhtw.seb.persistence.HistoryRepository;
import at.fhtw.seb.persistence.TournamentRepository;
import at.fhtw.seb.persistence.UserRepository;
import at.fhtw.seb.domain.*;
import at.fhtw.seb.persistence.*;
import at.fhtw.seb.presentation.handler.*;
import at.fhtw.seb.presentation.router.*;
import at.fhtw.seb.service.*;
import com.sun.net.httpserver.HttpServer;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.IOException;
import java.net.InetSocketAddress;

/*
 * Central server wiring — creates contexts and starts HttpServer.
 */
public class Server {
    private final int port = 10001;

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 10);

        PGSimpleDataSource ds = buildDataSource();

        // Infrastructure / persistence
        UserRepository userRepo = new UserRepository(ds);
        TournamentRepository tournamentRepo = new TournamentRepository(ds);
        HistoryRepository historyRepo = new HistoryRepository(ds);

        // Business / domain
        TokenManager tokenManager = new TokenManager();
        UserManager userManager = new UserManager(userRepo, tokenManager);
        TournamentManager tournamentManager =
                new TournamentManager(tournamentRepo, historyRepo, userRepo);
        StatsManager statsManager = new StatsManager(userRepo, historyRepo);

        // Application / service
        UserService userService = new UserService(userManager, tokenManager);
        HistoryService historyService = new HistoryService(tournamentManager, historyRepo);
        TournamentService tournamentService = new TournamentService(tournamentManager);
        StatsService statsService = new StatsService(statsManager);

        // Presentation
        UserRequestHandler userHandler = new UserRequestHandler(userService);
        HistoryRequestHandler historyHandler = new HistoryRequestHandler(historyService, userService);
        TournamentRequestHandler tournamentHandler =
                new TournamentRequestHandler(tournamentService, userService);
        StatsRequestHandler statsHandler = new StatsRequestHandler(statsService, userService);

        Router router = new Router(userHandler, historyHandler, statsHandler, tournamentHandler);
        SebHttpHandler httpHandler = new SebHttpHandler(router);

        // Single context "/" handles all routes; the Router dispatches internally.
        server.createContext("/", httpHandler);

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + port);
    }

    private PGSimpleDataSource buildDataSource() {
        String host = "localhost";
        String port = "5432";
        String database = "seb_project";
        String url = new StringBuilder("jdbc:postgresql://")
                .append(host)
                .append(":")
                .append(port)
                .append("/")
                .append(database)
                .toString();
        String username = "postgres";
        String password = "mysecretpassword";

        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(url);
        ds.setUser(username);
        ds.setPassword(password);
        return ds;
    }
}