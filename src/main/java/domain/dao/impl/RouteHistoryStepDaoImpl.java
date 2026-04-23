package domain.dao.impl;

import configuration.JdbcConnectionPool;
import domain.dao.RouteHistoryStepDao;
import domain.model.impl.RouteHistoryStep;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RouteHistoryStepDaoImpl implements RouteHistoryStepDao {
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

    private final JdbcConnectionPool jdbcConnectionPool;

    public RouteHistoryStepDaoImpl() {
        this.jdbcConnectionPool = JdbcConnectionPool.getInstance();
    }

    public RouteHistoryStepDaoImpl(JdbcConnectionPool jdbcConnectionPool) {
        this.jdbcConnectionPool = jdbcConnectionPool;
    }

    @Override
    public void save(RouteHistoryStep routeHistoryStep) {
        Connection connection = null;
        try {
            connection = jdbcConnectionPool.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL)) {
                preparedStatement.setString(1, routeHistoryStep.getId().toString());
                preparedStatement.setString(2, routeHistoryStep.getRouteHistoryId().toString());
                preparedStatement.setString(3, routeHistoryStep.getDotId().toString());
                preparedStatement.setInt(4, routeHistoryStep.getStepOrder());

                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save route history step with id " + routeHistoryStep.getId(), e
            );
        } finally {
            if (connection != null) {
                jdbcConnectionPool.releaseConnection(connection);
            }
        }
    }

    @Override
    public Optional<RouteHistoryStep> findById(UUID id) {
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
            throw new RuntimeException("Failed to find route history step by id " + id, e);
        } finally {
            if (connection != null) {
                jdbcConnectionPool.releaseConnection(connection);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns all steps for a route ordered by step_order.
     * Order is critical for reconstructing the route path.
     */
    @Override
    public List<RouteHistoryStep> findByRouteHistoryId(UUID routeHistoryId) {
        List<RouteHistoryStep> routeHistorySteps = new ArrayList<>();
        Connection connection = null;
        try {
            connection = jdbcConnectionPool.getConnection();
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ROUTE_HISTORY_ID_SQL)) {
                preparedStatement.setString(1, routeHistoryId.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        routeHistorySteps.add(mapRow(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find route history steps for route id " + routeHistoryId, e
            );
        } finally {
            if (connection != null) {
                jdbcConnectionPool.releaseConnection(connection);
            }
        }
        return routeHistorySteps;
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
