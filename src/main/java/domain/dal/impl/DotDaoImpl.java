package domain.dal.impl;

import domain.dal.DotDao;
import domain.dal.JdbcAware;
import domain.exception.DaoException;
import domain.model.impl.Dot;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DotDaoImpl extends JdbcAware implements DotDao {
    private static final String SAVE_SQL =
            "INSERT INTO Dot (id, name, x, y) VALUES (?, ?, ?, ?)";

    private static final String FIND_BY_ID_SQL =
            "SELECT id, name, x, y FROM Dot WHERE id = ?";

    private static final String FIND_ALL_SQL =
            "SELECT id, name, x, y FROM Dot";

    @Override
    public void save(Dot dot) {
        execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SAVE_SQL)) {
                preparedStatement.setObject(1, dot.getId());
                preparedStatement.setObject(2, dot.getName());
                preparedStatement.setObject(3, dot.getX());
                preparedStatement.setObject(4, dot.getY());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new DaoException("Failed to save dot with id " + dot.getId(), e);
            }
            return null;
        });
    }

    @Override
    public Optional<Dot> findById(UUID id) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_BY_ID_SQL)) {
                preparedStatement.setObject(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRow(resultSet));
                    }
                }
            } catch (SQLException e) {
                throw new DaoException("Failed to find dot by id " + id, e);
            }
            return Optional.empty();
        });
    }

    @Override
    public List<Dot> findAll() {
        return execute(connection -> {
            List<Dot> dots = new ArrayList<>();
            try (PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL_SQL);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    dots.add(mapRow(resultSet));
                }
            } catch (SQLException e) {
                throw new DaoException("Failed to find all dots", e);
            }
            return dots;
        });
    }

    private Dot mapRow(ResultSet resultSet) throws SQLException {
        return new Dot(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("name"),
                resultSet.getObject("x", Double.class),
                resultSet.getObject("y", Double.class)
        );
    }
}