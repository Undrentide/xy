package domain.dal.impl;

import domain.dal.JdbcAware;
import domain.dal.RouteHistoryStepDao;
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
            ORDER BY step_order
            """;

    @Override
    public void save(RouteHistoryStep routeHistoryStep) {
        execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL)) {
                preparedStatement.setObject(1, routeHistoryStep.getId());
                preparedStatement.setObject(2, routeHistoryStep.getRouteHistoryId());
                preparedStatement.setObject(3, routeHistoryStep.getDotId());
                preparedStatement.setObject(4, routeHistoryStep.getStepOrder());
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
                preparedStatement.setObject(1, id);
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

    @Override
    public List<RouteHistoryStep> findRouteHistoryStepListById(UUID routeHistoryId) {
        return execute(connection -> {
            List<RouteHistoryStep> routeHistorySteps = new ArrayList<>();
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ROUTE_HISTORY_ID_SQL)) {
                preparedStatement.setObject(1, routeHistoryId);
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

    private RouteHistoryStep mapRow(ResultSet resultSet) throws SQLException {
        return new RouteHistoryStep(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("route_history_id", UUID.class),
                resultSet.getObject("dot_id", UUID.class),
                resultSet.getObject("step_order", Integer.class)
        );
    }
}