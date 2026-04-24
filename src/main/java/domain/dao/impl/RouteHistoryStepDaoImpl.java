package domain.dao.impl;

import domain.dao.JdbcAware;
import domain.dao.RouteHistoryStepDao;
import domain.exception.DaoException;
import domain.model.impl.RouteHistoryStep;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RouteHistoryStepDaoImpl extends JdbcAware implements RouteHistoryStepDao {
    private static final String SAVE_SQL = """
            INSERT INTO RouteHistoryStep (id, route_history_id, dot_id, step_order)
            VALUES (?, ?, ?, ?)
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT id, route_history_id, dot_id, step_order
            FROM RouteHistoryStep
            WHERE id = ?
            """;

    private static final String FIND_BY_ROUTE_HISTORY_ID_SQL = """
            SELECT id, route_history_id, dot_id, step_order
            FROM RouteHistoryStep
            WHERE route_history_id = ?
            ORDER BY step_order ASC
            """;

    @Override
    public void save(RouteHistoryStep routeHistoryStep) {
        execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL)) {
                preparedStatement.setString(1, routeHistoryStep.getId().toString());
                preparedStatement.setString(2, routeHistoryStep.getRouteHistoryId().toString());
                preparedStatement.setString(3, routeHistoryStep.getDotId().toString());
                preparedStatement.setInt(4, routeHistoryStep.getStepOrder());

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new DaoException(
                        "Failed to save route history step with id " + routeHistoryStep.getId(), e
                );
            }
            return null;
        });
    }

    @Override
    public Optional<RouteHistoryStep> findById(UUID id) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
                preparedStatement.setString(1, id.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRow(resultSet));
                    }
                }
            } catch (SQLException e) {
                throw new DaoException("Failed to find route history step by id " + id, e);
            }
            return Optional.empty();
        });
    }

    /**
     * Returns all steps for a route ordered by step_order.
     * Order is critical for reconstructing the route path.
     */
    @Override
    public List<RouteHistoryStep> findByRouteHistoryId(UUID routeHistoryId) {
        return execute(connection -> {
            List<RouteHistoryStep> routeHistorySteps = new ArrayList<>();

            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ROUTE_HISTORY_ID_SQL)) {
                preparedStatement.setString(1, routeHistoryId.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        routeHistorySteps.add(mapRow(resultSet));
                    }
                }
            } catch (SQLException e) {
                throw new DaoException(
                        "Failed to find route history steps for route id " + routeHistoryId, e
                );
            }
            return routeHistorySteps;
        });
    }

    // Maps DB row to RouteHistoryStep entity
    private RouteHistoryStep mapRow(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        UUID routeHistoryId = UUID.fromString(resultSet.getString("route_history_id"));
        UUID dotId = UUID.fromString(resultSet.getString("dot_id"));
        Integer stepOrder = resultSet.getInt("step_order");

        return new RouteHistoryStep(id, routeHistoryId, dotId, stepOrder);
    }
}
