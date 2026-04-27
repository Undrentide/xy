package domain.dal;

import configuration.JdbcConnectionPool;
import domain.exception.DaoException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public abstract class JdbcAware {
    private static final JdbcConnectionPool jdbcConnectionPool =
            JdbcConnectionPool.getInstance();

    protected static <R> R execute(Function<Connection, R> action) {
        Connection connection = null;
        try {
            connection = jdbcConnectionPool.getConnection();
            return action.apply(connection);
        } catch (SQLException e) {
            throw new DaoException("Database error occurred", e);
        } finally {
            if (connection != null) {
                jdbcConnectionPool.releaseConnection(connection);
            }
        }
    }
}
