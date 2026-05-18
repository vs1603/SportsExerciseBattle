package at.fhtw.seb.domain;

import at.fhtw.seb.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserManagerTest {

    private UserRepository userRepo;
    private TokenManager tokenManager;
    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        tokenManager = new TokenManager();
        userManager = new UserManager(userRepo, tokenManager);
    }

    @Test
    void register_savesNewUserWithHashedPassword() {
        when(userRepo.findByUsername("alice")).thenReturn(null);
        when(userRepo.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1);
            return u;
        });

        User result = userManager.register("alice", "secret");

        assertEquals("alice", result.getUsername());
        assertNotEquals("secret", result.getPasswordHash());
        assertEquals(100, result.getElo());
    }

    @Test
    void register_throwsWhenUsernameAlreadyTaken() {
        when(userRepo.findByUsername("alice")).thenReturn(new User());

        assertThrows(IllegalStateException.class,
                () -> userManager.register("alice", "secret"));
    }

    @Test
    void register_throwsWhenUsernameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userManager.register("", "pass"));
    }

    @Test
    void register_throwsWhenPasswordIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> userManager.register("alice", ""));
    }

    @Test
    void login_returnsTokenOnSuccess() {
        User stored = new User(1, "alice", BCrypt.hashpw("secret", BCrypt.gensalt()),
                100, null, null, null);
        when(userRepo.findByUsername("alice")).thenReturn(stored);

        String token = userManager.login("alice", "secret");

        assertEquals("alice-sebToken", token);
        assertTrue(tokenManager.isValid(token));
    }

    @Test
    void login_throwsOnWrongPassword() {
        User stored = new User(1, "alice", BCrypt.hashpw("correct", BCrypt.gensalt()),
                100, null, null, null);
        when(userRepo.findByUsername("alice")).thenReturn(stored);

        assertThrows(IllegalArgumentException.class,
                () -> userManager.login("alice", "wrong"));
    }

    @Test
    void login_throwsWhenUserNotFound() {
        when(userRepo.findByUsername("ghost")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> userManager.login("ghost", "pass"));
    }

    @Test
    void updateProfile_throwsWhenRequesterIsNotOwner() {
        assertThrows(SecurityException.class,
                () -> userManager.updateProfile("alice", "bob", "Bob", "bio", "img"));
    }

    @Test
    void updateProfile_callsRepoForOwner() {
        User alice = new User(1, "alice", "hash", 100, null, null, null);
        when(userRepo.findByUsername("alice")).thenReturn(alice);

        userManager.updateProfile("alice", "alice", "Alice", "Bio", "img");

        verify(userRepo).updateProfile(any(User.class), eq(1));
    }

    @Test
    void logout_revokesToken() {
        when(userRepo.findByUsername("alice")).thenReturn(
                new User(1, "alice", BCrypt.hashpw("pass", BCrypt.gensalt()), 100, null, null, null));
        String token = userManager.login("alice", "pass");

        userManager.logout(token);

        assertFalse(tokenManager.isValid(token));
    }
}