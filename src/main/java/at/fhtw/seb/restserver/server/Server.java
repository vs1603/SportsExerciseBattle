package at.fhtw.seb.restserver.server;

import at.fhtw.seb.persistence.HistoryRepository;
import at.fhtw.seb.persistence.TournamentRepository;
import at.fhtw.seb.persistence.UserRepository;
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

//        // Business / domain
//        MediaManager manager = new MediaManager(repo);
//
//        // Application / service
//        MediaService service = new MediaService(manager);
//
//        // Presentation
//        MediaRequestHandler requestHandler = new MediaRequestHandler(service);
//        MediaRouter router = new MediaRouter(requestHandler);
//        MediaHttpHandler httpHandler = new MediaHttpHandler(router);
//
//        // contexts:
//        // /media      -> list & create
//        // /media/{id} -> get & delete & update
//        server.createContext("/media", httpHandler);

        server.setExecutor(null);
        server.start();
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