package at.fhtw.seb.restserver.server;

import com.sun.net.httpserver.HttpExchange;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Request {
    private String urlContent;
    private String pathname;
    private List<String> pathParts;
    private String params;
    private Map<String, List<String>> headers;

    public Request(HttpExchange exchange) {
        URI url = exchange.getRequestURI();
        this.setUrlContent(url.toString());
        this.setPathname(url.getPath());
        this.setParams(url.getQuery());
        this.headers = exchange.getRequestHeaders();
    }

    public String getUrlContent(){
        return this.urlContent;
    }

    private void setUrlContent(String urlContent) {
        this.urlContent = urlContent;
    }

    public String getPathname() {
        return pathname;
    }

    private void setPathname(String pathname) {
        this.pathname = pathname;
        String[] stringParts = pathname.split("/");
        this.pathParts = new ArrayList<>();
        for (String part : stringParts)
        {
            if (part != null &&
                    !part.isEmpty())
            {
                this.pathParts.add(part);
            }
        }
    }

    public String getParams() {
        return params;
    }

    private void setParams(String params) {
        this.params = params;
    }

    public List<String> getPathParts() {
        return pathParts;
    }

    private void setPathParts(List<String> pathParts) {
        this.pathParts = pathParts;
    }

    /* Helper to get specific header like "Authorization" */
    public String getHeader(String name) {
        List<String> values = headers.get(name);
        if (values != null && !values.isEmpty()) {
            return values.getFirst();
        }
        return null;
    }

//    Extracts the token from the "Authorization: Bearer <token>" header.
    public String getBearerToken() {
        String auth = getHeader("Authorization");
        if (auth == null) return null;
        if (auth.startsWith("Bearer ")) {
            return auth.substring("Bearer ".length()).trim();
        }
        return null;
    }
}










