package domain.dao.impl;

import configuration.JdbcConnectionPool;
import domain.dao.RouteHistoryDao;
import domain.model.impl.RouteHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RouteHistoryDaoImpl implements RouteHistoryDao {
    private static final String SAVE_SQL = """
            INSERT INTO RouteHistory (id, start_dot_id, end_dot_id, created_at)
            VALUES (?, ?, ?, ?)
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT id, start_dot_id, end_dot_id, created_at
            FROM RouteHistory
            WHERE id = ?
            """;

    private static final String FIND_LAST_ROUTES_SQL = """
            SELECT id, start_dot_id, end_dot_id, created_at
            FROM RouteHistory
            ORDER BY created_at DESC
            LIMIT ?
            """;

    private final JdbcConnectionPool jdbcConnectionPool;

    public RouteHistoryDaoImpl() {
        this.jdbcConnectionPool = JdbcConnectionPool.getInstance();
    }

    public RouteHistoryDaoImpl(JdbcConnectionPool jdbcConnectionPool) {
        this.jdbcConnectionPool = jdbcConnectionPool;
    }

    @Override
    public void save(RouteHistory routeHistory) {
        Connection connection = null;
        try {
            connection = jdbcConnectionPool.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL)) {
                preparedStatement.setString(1, routeHistory.getId().toString());
                preparedStatement.setString(2, routeHistory.getStartDotId().toString());
                preparedStatement.setString(3, routeHistory.getEndDotId().toString());
                preparedStatement.setTimestamp(4, Timestamp.from(routeHistory.getCreatedAt()));

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save route history with id " + routeHistory.getId(), e);
        } finally {
            if (connection != null) {
                jdbcConnectionPool.releaseConnection(connection);
            }
        }
    }

    @Override
    public Optional<RouteHistory> findById(UUID id) {
        Connection connection = null;
        try {
            connection = jdbcConnectionPool.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
                preparedStatement.setString(1, id.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRow(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find route history by id " + id, e);
        } finally {
            if (connection != null) {
                jdbcConnectionPool.releaseConnection(connection);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<RouteHistory> findLastRoutes(int limit) {
        List<RouteHistory> routeHistories = new ArrayList<>();
        Connection connection = null;
        try {
            connection = jdbcConnectionPool.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_LAST_ROUTES_SQL)) {
                preparedStatement.setInt(1, limit);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        routeHistories.add(mapRow(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find last route history records", e);
        } finally {
            if (connection != null) {
                jdbcConnectionPool.releaseConnection(connection);
            }
        }
        return routeHistories;
    }

    // Maps DB row to RouteHistory entity
    private RouteHistory mapRow(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        UUID startDotId = UUID.fromString(resultSet.getString("start_dot_id"));
        UUID endDotId = UUID.fromString(resultSet.getString("end_dot_id"));
        Instant createdAt = resultSet.getTimestamp("created_at").toInstant();

        return new RouteHistory(id, startDotId, endDotId, createdAt);
    }
}
