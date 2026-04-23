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
import java.util.UUID;

public class RouteHistoryStepDaoImpl implements RouteHistoryStepDao {
    private final JdbcConnectionPool connectionPool;

    public RouteHistoryStepDaoImpl() {
        this.connectionPool = JdbcConnectionPool.getInstance();
    }

    public RouteHistoryStepDaoImpl(JdbcConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void save(RouteHistoryStep routeHistoryStep) {
        String sql = """
                INSERT INTO RouteHistoryStep (id, route_history_id, dot_id, step_order)
                VALUES (?, ?, ?, ?)
                """;

        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, routeHistoryStep.getId().toString());
                statement.setString(2, routeHistoryStep.getRouteHistoryId().toString());
                statement.setString(3, routeHistoryStep.getDotId().toString());
                statement.setInt(4, routeHistoryStep.getStepOrder());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save route history step with id " + routeHistoryStep.getId(), e
            );
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    /**
     * Returns all steps for a route ordered by step_order.
     * Order is critical for reconstructing the route path.
     */
    @Override
    public List<RouteHistoryStep> findByRouteHistoryId(UUID routeHistoryId) {
        String sql = """
                SELECT id, route_history_id, dot_id, step_order
                FROM RouteHistoryStep
                WHERE route_history_id = ?
                ORDER BY step_order ASC
                """;

        List<RouteHistoryStep> steps = new ArrayList<>();
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, routeHistoryId.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        steps.add(mapRow(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to find route history steps for route id " + routeHistoryId, e
            );
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
        return steps;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM RouteHistoryStep";

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
            throw new RuntimeException("Failed to count route history steps", e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
        return 0;
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
