package at.fhtw.seb.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenManager {

    private final Map<String, String> validTokens = new ConcurrentHashMap<>();

    /** Generates a token and stores it */
    public String generateToken(String username) {
        String token = username + "-sebToken";
        validTokens.put(token, username);
        return token;
    }

    /** Validates a token */
    public boolean isValid(String token) {
        return validTokens.containsKey(token);
    }

    /** Gets the username for a token */
    public String getUsername(String token) {
        return validTokens.get(token);
    }

    /** Invalidates a token (e.g., logout) */
    public void revokeToken(String token) {
        validTokens.remove(token);
    }
}