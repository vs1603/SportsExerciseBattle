package at.fhtw.seb.presentation.router;

import at.fhtw.seb.restserver.http.ContentType;
import at.fhtw.seb.restserver.http.HttpStatus;
import at.fhtw.seb.restserver.server.Request;
import at.fhtw.seb.restserver.server.Response;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;

public class SebHttpHandler implements HttpHandler {

    private final Router router;

    public SebHttpHandler(Router router) {
        this.router = router;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            Request request = new Request(exchange);
            String method = exchange.getRequestMethod();
            String body = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);

            Response response = router.route(request, method, body);
            response.send(exchange);

        } catch (Exception e) {
            System.err.println("[SebHttpHandler] Unhandled error: " + e.getMessage());
            e.printStackTrace();
            // Send 500
            try {
                new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
                        "{\"message\":\"Internal server error.\"}").send(exchange);
            } catch (Exception ignored) {}
        }
    }
}