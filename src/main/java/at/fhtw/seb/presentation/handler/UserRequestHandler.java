package at.fhtw.seb.presentation.handler;

import at.fhtw.seb.domain.User;
import at.fhtw.seb.presentation.dtos.*;
import at.fhtw.seb.restserver.http.ContentType;
import at.fhtw.seb.restserver.http.HttpStatus;
import at.fhtw.seb.restserver.server.Response;
import at.fhtw.seb.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

// Handles HTTP requests for the /users and /sessions endpoints.
// POST   /users            register
// GET    /users/{username} get profile
// PUT    /users/{username} update profile
// POST   /sessions         login

public class UserRequestHandler {

    private final UserService userService;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserRequestHandler(UserService userService) {
        this.userService = userService;
    }

    // POST /users
    public Response handleRegister(String body) throws Exception {
        CredentialsDto dto = mapper.readValue(body, CredentialsDto.class);
        try {
            User user = userService.register(dto.getUsername(), dto.getPassword());
            String json = mapper.writeValueAsString(
                    new MessageDto("User '" + user.getUsername() + "' registered successfully."));
            return new Response(HttpStatus.CREATED, ContentType.JSON, json);
        } catch (IllegalStateException e) {
            // duplicate username
            return errorResponse(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // POST /sessions
    public Response handleLogin(String body) throws Exception {
        CredentialsDto dto = mapper.readValue(body, CredentialsDto.class);
        try {
            String token = userService.login(dto.getUsername(), dto.getPassword());
            String json = mapper.writeValueAsString(
                    new TokenResponseDto(token, "Login successful."));
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    // GET /users/{username}
    public Response handleGetUser(String targetUsername, String requesterToken) throws Exception {
        if (!userService.isTokenValid(requesterToken)) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        String requesterUsername = userService.resolveToken(requesterToken);
        // Users may only read their own profile (or admin – not implemented here)
        if (!requesterUsername.equals(targetUsername)) {
            return errorResponse(HttpStatus.FORBIDDEN,
                    "You are not allowed to view this profile.");
        }
        try {
            User user = userService.getByUsername(targetUsername);
            UserResponseDto dto = new UserResponseDto(
                    user.getUsername(), user.getName(), user.getBio(),
                    user.getImage(), user.getElo());
            return new Response(HttpStatus.OK, ContentType.JSON, mapper.writeValueAsString(dto));
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // PUT /users/{username}
    public Response handleUpdateUser(String targetUsername, String requesterToken,
                                     String body) throws Exception {
        if (!userService.isTokenValid(requesterToken)) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        String requesterUsername = userService.resolveToken(requesterToken);
        ProfileDto dto = mapper.readValue(body, ProfileDto.class);
        try {
            userService.updateProfile(targetUsername, requesterUsername,
                    dto.getName(), dto.getBio(), dto.getImage());
            String json = mapper.writeValueAsString(
                    new MessageDto("Profile updated for '" + targetUsername + "'."));
            return new Response(HttpStatus.OK, ContentType.JSON, json);
        } catch (SecurityException e) {
            return errorResponse(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // DELETE /sessions
    public Response handleLogout(String token) throws Exception {
        if (!userService.isTokenValid(token)) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Invalid or missing token.");
        }
        userService.logout(token);
        String json = mapper.writeValueAsString(new MessageDto("Logout successful."));
        return new Response(HttpStatus.OK, ContentType.JSON, json);
    }

    // ---- Helper ----

    private Response errorResponse(HttpStatus status, String message) throws Exception {
        String json = mapper.writeValueAsString(new MessageDto(message));
        return new Response(status, ContentType.JSON, json);
    }
}