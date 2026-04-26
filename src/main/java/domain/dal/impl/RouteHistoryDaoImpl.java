package domain.dal.impl;

import domain.dal.JdbcAware;
import domain.dal.RouteHistoryDao;
import domain.exception.DaoException;
import domain.model.impl.RouteHistory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
                preparedStatement.setObject(1, routeHistory.getId());
                preparedStatement.setObject(2, routeHistory.getStartDotId());
                preparedStatement.setObject(3, routeHistory.getEndDotId());
                preparedStatement.setObject(4, routeHistory.getCreatedAt());
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
                preparedStatement.setObject(1, id);
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

    private RouteHistory mapRow(ResultSet resultSet) throws SQLException {
        return new RouteHistory(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("start_dot_id", UUID.class),
                resultSet.getObject("end_dot_id", UUID.class),
                resultSet.getTimestamp("created_at").toInstant()
        );
    }
}