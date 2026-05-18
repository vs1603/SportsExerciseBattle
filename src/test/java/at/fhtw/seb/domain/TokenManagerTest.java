package at.fhtw.seb.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenManagerTest {

    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        tokenManager = new TokenManager();
    }

    @Test
    void generateToken_followsExpectedPattern() {
        String token = tokenManager.generateToken("alice");
        assertEquals("alice-sebToken", token);
    }

    @Test
    void generateToken_isValid() {
        String token = tokenManager.generateToken("alice");
        assertTrue(tokenManager.isValid(token));
    }

    @Test
    void isValid_returnsFalseForUnknownToken() {
        assertFalse(tokenManager.isValid("nobody-sebToken"));
    }

    @Test
    void isValid_returnsFalseForNull() {
        assertFalse(tokenManager.isValid(null));
    }

    @Test
    void getUsername_returnsCorrectUsername() {
        tokenManager.generateToken("bob");
        assertEquals("bob", tokenManager.getUsername("bob-sebToken"));
    }

    @Test
    void revokeToken_tokenIsNoLongerValid() {
        String token = tokenManager.generateToken("chris");
        tokenManager.revokeToken(token);
        assertFalse(tokenManager.isValid(token));
    }

    @Test
    void multipleUsers_canHaveTokensSimultaneously() {
        tokenManager.generateToken("alice");
        tokenManager.generateToken("bob");
        assertTrue(tokenManager.isValid("alice-sebToken"));
        assertTrue(tokenManager.isValid("bob-sebToken"));
    }
}