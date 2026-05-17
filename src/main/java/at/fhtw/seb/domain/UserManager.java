package at.fhtw.seb.domain;

import at.fhtw.seb.persistence.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserManager {

    private static final int INITIAL_ELO = 100;

    private final UserRepository userRepo;
    private final TokenManager tokenManager;

    public UserManager(UserRepository userRepo, TokenManager tokenManager) {
        this.userRepo = userRepo;
        this.tokenManager = tokenManager;
    }

    public User register(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (userRepo.findByUsername(username) != null) {
            throw new IllegalStateException("Username '" + username + "' is already taken");
        }
        User user = new User(null, username, BCrypt.hashpw(password, BCrypt.gensalt()),
                INITIAL_ELO, null, null, null);
        User saved = userRepo.save(user);
        System.out.println("[UserManager] Registered user: " + username);
        return saved;
    }

    public String login(String username, String password) {
        User user = userRepo.findByUsername(username);
        if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials for user: " + username);
        }
        String token = tokenManager.generateToken(username);
        System.out.println("[UserManager] Login successful: " + username + " -> " + token);
        return token;
    }

    public void logout(String token) {
        String username = tokenManager.getUsername(token);
        tokenManager.revokeToken(token);
        System.out.println("[UserManager] Logged out: " + username);
    }

    public User getByUsername(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        return user;
    }

    public List<User> getAll() {
        return userRepo.findAll();
    }

    public void updateProfile(String targetUsername, String requesterUsername,
                              String name, String bio, String image) {
        if (!targetUsername.equals(requesterUsername)) {
            throw new SecurityException(
                    "User '" + requesterUsername + "' is not allowed to edit '" + targetUsername + "'");
        }
        User existing = userRepo.findByUsername(targetUsername);
        if (existing == null) {
            throw new IllegalArgumentException("User not found: " + targetUsername);
        }

        existing.setName(name);
        existing.setBio(bio);
        existing.setImage(image);

        userRepo.updateProfile(existing, existing.getId());
        System.out.println("[UserManager] Profile updated for: " + targetUsername);
    }
}