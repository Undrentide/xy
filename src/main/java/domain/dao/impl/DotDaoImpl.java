package domain.dao.impl;

import domain.dao.DotDao;
import domain.dao.JdbcAware;
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
                preparedStatement.setString(1, dot.getId().toString());
                preparedStatement.setString(2, dot.getName());
                preparedStatement.setDouble(3, dot.getX());
                preparedStatement.setDouble(4, dot.getY());

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
                preparedStatement.setString(1, id.toString());
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

    // Maps DB row to Dot entity
    private Dot mapRow(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        String name = resultSet.getString("name");
        Double x = resultSet.getDouble("x");
        Double y = resultSet.getDouble("y");

        return new Dot(id, name, x, y);
    }
}
