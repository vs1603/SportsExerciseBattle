package at.fhtw.seb.persistence;

import at.fhtw.seb.domain.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository implements Repository<User> {

    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        initTable();
    }

    private void initTable() {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id            SERIAL PRIMARY KEY,
                    username      VARCHAR(100) NOT NULL UNIQUE,
                    password_hash VARCHAR(64)  NOT NULL,
                    elo           INT          NOT NULL DEFAULT 100,
                    name          VARCHAR(255),
                    bio           TEXT,
                    image         VARCHAR(255)
                )
                """;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()
        ) {
            stmt.execute(createUsersTable);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing user table", e);
        }
    }

    @Override
    public User save(User user) {
        final String INSERT_USER =
                "INSERT INTO users (username, password_hash, elo, name, bio, image) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setInt(3, user.getElo());
            ps.setString(4, user.getName());
            ps.setString(5, user.getBio());
            ps.setString(6, user.getImage());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
                else {
                    throw new SQLException("No generated key returned after INSERT");
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
        return user;
    }

    @Override
    public User findById(Integer id) {
        final String selectUser = "SELECT * FROM users WHERE id = ?";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(selectUser)
        ) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return null;

            return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getInt("elo"),
                    rs.getString("name"),
                    rs.getString("bio"),
                    rs.getString("image")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user by id", e);
        }
    }

    public User findByUsername(String username) {
        final String selectUser = "SELECT * FROM users WHERE username = ?";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(selectUser)
        ) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return null;

            return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getInt("elo"),
                    rs.getString("name"),
                    rs.getString("bio"),
                    rs.getString("image")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching user by username", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        final String selectAll = "SELECT * FROM users ORDER BY elo DESC";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectAll)
        ) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getInt("elo"),
                        rs.getString("name"),
                        rs.getString("bio"),
                        rs.getString("image")
                        ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all users", e);
        }
        return users;
    }

    public User updateProfile(User user, Integer id) {
        final String UPDATE_USER =
                "UPDATE users SET name = ?, bio = ?, image = ? WHERE id = ?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(UPDATE_USER)
        ) {
            conn.setAutoCommit(false);
            ps.setString(1, user.getName());
            ps.setString(2, user.getBio());
            ps.setString(3, user.getImage());
            ps.setInt(4, user.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No person found with id " + id);
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
        user.setId(id);
        return user;
    }

    @Override
    public User delete(Integer id) {
        User user = findById(id);
        if (user == null)
            throw new RuntimeException("User with id " + id + " does not exist");

        final String sql = "DELETE FROM users WHERE id = ?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
        return user;
    }
}