package domain.dao.impl;

import configuration.JdbcConnectionPool;
import domain.dao.RouteHistoryDao;
import domain.model.impl.RouteHistory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RouteHistoryDaoImpl implements RouteHistoryDao {
    private final JdbcConnectionPool connectionPool;

    public RouteHistoryDaoImpl() {
        this.connectionPool = JdbcConnectionPool.getInstance();
    }

    public RouteHistoryDaoImpl(JdbcConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void save(RouteHistory routeHistory) {
        String sql = """
                INSERT INTO RouteHistory (id, start_dot_id, end_dot_id, created_at)
                VALUES (?, ?, ?, ?)
                """;

        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, routeHistory.getId().toString());
                statement.setString(2, routeHistory.getStartDotId().toString());
                statement.setString(3, routeHistory.getEndDotId().toString());
                statement.setTimestamp(4, Timestamp.valueOf(routeHistory.getCreatedAt()));

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save route history with id " + routeHistory.getId(), e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    @Override
    public Optional<RouteHistory> findById(UUID id) {
        String sql = """
                SELECT id, start_dot_id, end_dot_id, created_at
                FROM RouteHistory
                WHERE id = ?
                """;

        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, id.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRow(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find route history by id " + id, e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<RouteHistory> findLastRoutes(int limit) {
        String sql = """
                SELECT id, start_dot_id, end_dot_id, created_at
                FROM RouteHistory
                ORDER BY created_at DESC
                LIMIT ?
                """;

        List<RouteHistory> routes = new ArrayList<>();
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, limit);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        routes.add(mapRow(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find last route history records", e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
        return routes;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM RouteHistory";

        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count route history records", e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
        return 0;
    }

    // Maps DB row to RouteHistory entity
    private RouteHistory mapRow(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        UUID startDotId = UUID.fromString(resultSet.getString("start_dot_id"));
        UUID endDotId = UUID.fromString(resultSet.getString("end_dot_id"));
        LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();

        return new RouteHistory(id, startDotId, endDotId, createdAt);
    }
}
