package domain.dao.impl;

import configuration.JdbcConnectionPool;
import domain.dao.DotDao;
import domain.model.impl.Dot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DotDaoImpl implements DotDao {
    private final JdbcConnectionPool connectionPool;

    public DotDaoImpl() {
        this.connectionPool = JdbcConnectionPool.getInstance();
    }

    public DotDaoImpl(JdbcConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void save(Dot dot) {
        String sql = "INSERT INTO Dot (id, name, x, y) VALUES (?, ?, ?, ?)";

        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, dot.getId().toString());
                statement.setString(2, dot.getName());
                statement.setDouble(3, dot.getX());
                statement.setDouble(4, dot.getY());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save dot with id " + dot.getId(), e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }

    @Override
    public Optional<Dot> findById(UUID id) {
        String sql = "SELECT id, name, x, y FROM Dot WHERE id = ?";

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
            throw new RuntimeException("Failed to find dot by id " + id, e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Dot> findAll() {
        String sql = "SELECT id, name, x, y FROM Dot";

        List<Dot> dots = new ArrayList<>();
        Connection connection = null;

        try {
            connection = connectionPool.getConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    dots.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all dots", e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
        return dots;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM Dot";

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
            throw new RuntimeException("Failed to count dots", e);
        } finally {
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
        return 0;
    }

    // Maps DB row to Dot entity
    private Dot mapRow(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        String name = resultSet.getString("name");
        Double x = resultSet.getDouble("x");
        Double y = resultSet.getDouble("y");

        return new Dot(id, name, x, y);
    }
}
