package at.fhtw.seb.persistence;

import at.fhtw.seb.domain.HistoryEntry;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryRepository implements Repository<HistoryEntry> {

    private final DataSource dataSource;

    public HistoryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        initTable();
    }

    private void initTable() {
        String createHistoryTable = """
                CREATE TABLE IF NOT EXISTS history (
                                     id                  SERIAL PRIMARY KEY,
                                     user_id             INT REFERENCES users(id) ON DELETE CASCADE,
                                     tournament_id       INT REFERENCES tournaments(id),
                                     count               INT NOT NULL,
                                     duration            INT NOT NULL,
                                     recorded_at         TIMESTAMP NOT NULL DEFAULT NOW()
                                 )
                """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()
        ) {
            stmt.execute(createHistoryTable);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing history table", e);
        }
    }

    @Override
    public HistoryEntry save(HistoryEntry entry) {
        final String sql =
                "INSERT INTO history (user_id, tournament_id, count, duration, recorded_at) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false);
            ps.setInt(1, entry.getUserId());
            if (entry.getTournamentId() != null) {
                ps.setInt(2, entry.getTournamentId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, entry.getCount());
            ps.setInt(4, entry.getDuration());
            ps.setTimestamp(5, Timestamp.valueOf(entry.getRecordedAt()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next())
                    entry.setId(keys.getInt(1));
                else {
                    throw new SQLException("No generated key returned after INSERT");
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving history entry", e);
        }
        return entry;
    }

    @Override
    public HistoryEntry findById(Integer id) {
        final String sql = "SELECT * FROM history WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;

            return new HistoryEntry(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getObject("tournament_id", Integer.class),
                    rs.getInt("count"),
                    rs.getInt("duration"),
                    rs.getTimestamp("recorded_at").toLocalDateTime()

            );
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching history entry by id", e);
        }
    }

    @Override
    public List<HistoryEntry> findAll() {
        List<HistoryEntry> list = new ArrayList<>();
        final String sql = "SELECT * FROM history ORDER BY recorded_at DESC";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)
        ) {
            while (rs.next()) {
                list.add(new HistoryEntry(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getObject("tournament_id", Integer.class),
                        rs.getInt("count"),
                        rs.getInt("duration"),
                        rs.getTimestamp("recorded_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching history entries", e);
        }
        return list;
    }

    public List<HistoryEntry> findByUserId(int userId) {
        List<HistoryEntry> list = new ArrayList<>();
        final String sql =
                "SELECT * FROM history WHERE user_id = ? ORDER BY recorded_at DESC";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
             ) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new HistoryEntry(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getObject("tournament_id", Integer.class),
                        rs.getInt("count"),
                        rs.getInt("duration"),
                        rs.getTimestamp("recorded_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching history entries for user", e);
        }
        return list;
    }

    public List<HistoryEntry> findByTournamentId(int tournamentId) {
        List<HistoryEntry> list = new ArrayList<>();
        final String sql =
                "SELECT * FROM history WHERE tournament_id = ? ORDER BY recorded_at DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new HistoryEntry(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getObject("tournament_id", Integer.class),
                        rs.getInt("count"),
                        rs.getInt("duration"),
                        rs.getTimestamp("recorded_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching history entries for tournament", e);
        }
        return list;
    }

    @Override
    public HistoryEntry delete(Integer id) {
        HistoryEntry entry = findById(id);
        if (entry == null)
            throw new RuntimeException("History entry with id " + id + " does not exist");

        final String sql = "DELETE FROM history WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting history entry", e);
        }
        return entry;
    }
}