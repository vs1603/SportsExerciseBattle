package at.fhtw.seb.persistence;

import at.fhtw.seb.domain.Tournament;
import at.fhtw.seb.domain.TournamentState;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentRepository implements Repository<Tournament> {

    private final DataSource dataSource;

    public TournamentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        initTable();
    }

    private void initTable() {
        String createTournamentsTable = """
                CREATE TABLE IF NOT EXISTS tournaments (
                                    id         SERIAL PRIMARY KEY,
                                    start_time TIMESTAMP NOT NULL,
                                    end_time   TIMESTAMP NOT NULL,
                                    state      VARCHAR(20) NOT NULL DEFAULT 'RUNNING'
                                )
                """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()
        ) {
            stmt.execute(createTournamentsTable);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing tournaments table", e);
        }
    }

    @Override
    public Tournament save(Tournament tournament) {
        final String sql =
                "INSERT INTO tournaments (start_time, end_time, state) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false);
            ps.setTimestamp(1, Timestamp.valueOf(tournament.getStartTime()));
            ps.setTimestamp(2, Timestamp.valueOf(tournament.getEndTime()));
            ps.setString(3, tournament.getState().name());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tournament.setId(rs.getInt(1));
                }
                else {
                    throw new SQLException("No generated key returned after INSERT");
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving tournament", e);
        }
        return tournament;
    }

    @Override
    public Tournament findById(Integer id) {
        final String sql = "SELECT * FROM tournaments WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;

            return new Tournament(
                rs.getInt("id"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime(),
                TournamentState.valueOf(rs.getString("state"))
            );
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tournament by id", e);
        }
    }

    @Override
    public List<Tournament> findAll() {
        List<Tournament> list = new ArrayList<>();
        final String sql = "SELECT * FROM tournaments ORDER BY start_time DESC";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                list.add(new Tournament(
                        rs.getInt("id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime(),
                        TournamentState.valueOf(rs.getString("state"))
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching tournaments", e);
        }
        return list;
    }

    public Tournament markEnded(Integer id) {
        Tournament tournament = findById(id);
        if (tournament == null) {
            throw new RuntimeException("Tournament with id " + id + " does not exist");
        }
        final String sql = "UPDATE tournament SET state = 'ENDED' WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error ending tournament", e);
        }
        return tournament;
    }

    @Override
    public Tournament delete(Integer id) {
        Tournament tournament = findById(id);
        if (tournament == null) {
            throw new RuntimeException("Tournament with id " + id + " does not exist");
        }
        final String sql = "DELETE FROM tournaments WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting tournament", e);
        }
        return tournament;
    }
}
