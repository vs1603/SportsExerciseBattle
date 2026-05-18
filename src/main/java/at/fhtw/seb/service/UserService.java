package at.fhtw.seb.service;

import at.fhtw.seb.domain.*;

import java.util.List;

public class UserService {

    private final UserManager userManager;
    private final TokenManager tokenManager;

    public UserService(UserManager userManager, TokenManager tokenManager) {
        this.userManager = userManager;
        this.tokenManager = tokenManager;
    }

    public User register(String username, String password) {
        return userManager.register(username, password);
    }

    public String login(String username, String password) {
        return userManager.login(username, password);
    }

    public User getByUsername(String username) {
        return userManager.getByUsername(username);
    }

    public List<User> getAll() {
        return userManager.getAll();
    }

    public void updateProfile(String targetUsername, String requesterUsername,
                              String name, String bio, String image) {
        userManager.updateProfile(targetUsername, requesterUsername, name, bio, image);
    }

    public String resolveToken(String token) {
        return tokenManager.getUsername(token);
    }

    public boolean isTokenValid(String token) {
        return tokenManager.isValid(token);
    }

    public void logout(String token) {
        userManager.logout(token);
    }
}