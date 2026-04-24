package domain.dao.impl;

import domain.dao.JdbcAware;
import domain.dao.RouteHistoryDao;
import domain.exception.DaoException;
import domain.model.impl.RouteHistory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RouteHistoryDaoImpl extends JdbcAware implements RouteHistoryDao {
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

    @Override
    public void save(RouteHistory routeHistory) {
        execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL)) {
                preparedStatement.setString(1, routeHistory.getId().toString());
                preparedStatement.setString(2, routeHistory.getStartDotId().toString());
                preparedStatement.setString(3, routeHistory.getEndDotId().toString());
                preparedStatement.setTimestamp(4, Timestamp.from(routeHistory.getCreatedAt()));

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new DaoException("Failed to save route history with id " + routeHistory.getId(), e);
            }
            return null;
        });
    }

    @Override
    public Optional<RouteHistory> findById(UUID id) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
                preparedStatement.setString(1, id.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRow(resultSet));
                    }
                }
            } catch (SQLException e) {
                throw new DaoException("Failed to find route history by id " + id, e);
            }
            return Optional.empty();
        });
    }

    @Override
    public List<RouteHistory> findLastRoutes(int limit) {
        return execute(connection -> {
            List<RouteHistory> routeHistories = new ArrayList<>();

            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_LAST_ROUTES_SQL)) {
                preparedStatement.setInt(1, limit);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        routeHistories.add(mapRow(resultSet));
                    }
                }
            } catch (SQLException e) {
                throw new DaoException("Failed to find last route history records", e);
            }
            return routeHistories;
        });
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
