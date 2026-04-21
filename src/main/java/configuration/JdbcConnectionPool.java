package configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class JdbcConnectionPool implements AutoCloseable {
    private static JdbcConnectionPool instance;
    private final DatabaseConfiguration configuration;
    private final Deque<Connection> availableConnections;
    private final List<Connection> borrowedConnections;
    private boolean closed;

    private JdbcConnectionPool(DatabaseConfiguration databaseConfiguration) {
        this.configuration = databaseConfiguration;
        this.availableConnections = new ArrayDeque<>();
        this.borrowedConnections = new ArrayList<>();
        loadDriver();
        initializeConnections();
    }

    public static JdbcConnectionPool getInstance() {
        if (instance == null) {
            instance = new JdbcConnectionPool(DatabaseConfiguration.load());
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        ensureOpen();
        if (availableConnections.isEmpty()) {
            throw new SQLException("No available connections in the pool.");
        }
        Connection connection = availableConnections.removeFirst();
        if (isInvalid(connection)) {
            closeSilently(connection);
            connection = createConnection();
        }
        borrowedConnections.add(connection);
        return connection;
    }

    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        if (!borrowedConnections.remove(connection)) {
            throw new IllegalArgumentException("Connection was not borrowed from this pool.");
        }
        if (closed) {
            closeSilently(connection);
            return;
        }
        try {
            resetConnection(connection);
            if (isInvalid(connection)) {
                closeSilently(connection);
                availableConnections.addLast(createConnection());
                return;
            }
            availableConnections.addLast(connection);
        } catch (SQLException e) {
            closeSilently(connection);
            throw new IllegalStateException("Failed to return connection to pool.", e);
        }
    }

    @Override
    public void close() {
        shutdown();
    }

    public void shutdown() {
        if (closed) {
            return;
        }
        closed = true;
        for (Connection connection : availableConnections) {
            closeSilently(connection);
        }
        for (Connection connection : borrowedConnections) {
            closeSilently(connection);
        }
        availableConnections.clear();
        borrowedConnections.clear();
        instance = null;
    }

    private void loadDriver() {
        try {
            Class.forName(configuration.driver());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load JDBC driver.", e);
        }
    }

    private void initializeConnections() {
        try {
            for (int i = 0; i < configuration.poolSize(); i++) {
                availableConnections.addLast(createConnection());
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize connection pool.", e);
        }
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
                configuration.url(),
                configuration.username(),
                configuration.password()
        );
    }

    private void ensureOpen() throws SQLException {
        if (closed) {
            throw new SQLException("Connection pool is closed.");
        }
    }

    private void resetConnection(Connection connection) throws SQLException {
        if (!connection.getAutoCommit()) {
            connection.rollback();
            connection.setAutoCommit(true);
        }
        if (connection.isReadOnly()) {
            connection.setReadOnly(false);
        }
    }

    private boolean isInvalid(Connection connection) {
        try {
            return connection == null || connection.isClosed() || !connection.isValid(2);
        } catch (SQLException e) {
            return true;
        }
    }

    private void closeSilently(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}