package configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record DatabaseConfiguration(String driver, String url, String username, String password, int poolSize) {
    private static final String RESOURCE_NAME = "database.properties";

    public static DatabaseConfiguration load() {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(RESOURCE_NAME)) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing resource: " + RESOURCE_NAME);
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load database configuration.", e);
        }
        return new DatabaseConfiguration(
                resolve(properties, "db.driver", "DB_DRIVER"),
                resolve(properties, "db.url", "DB_URL"),
                resolve(properties, "db.username", "DB_USERNAME"),
                resolve(properties, "db.password", "DB_PASSWORD"),
                resolveInt(properties)
        );
    }

    private static String resolve(Properties properties, String propertyKey, String envKey) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String propertyValue = properties.getProperty(propertyKey);
        if (propertyValue == null || propertyValue.isBlank()) {
            throw new IllegalStateException("Missing database configuration property: " + propertyKey);
        }
        return propertyValue.trim();
    }

    private static int resolveInt(Properties properties) {
        String value = resolve(properties, "db.pool.size", "DB_POOL_SIZE");
        try {
            int parsedValue = Integer.parseInt(value);
            if (parsedValue < 1) {
                throw new IllegalStateException("Database pool size must be greater than zero.");
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid database pool size: " + value, e);
        }
    }
}