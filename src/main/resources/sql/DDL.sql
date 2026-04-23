CREATE SCHEMA IF NOT EXISTS xy;
USE xy;

CREATE TABLE IF NOT EXISTS Dot (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL
    );

CREATE TABLE IF NOT EXISTS RouteHistory (
    id VARCHAR(36) PRIMARY KEY,
    start_dot_id VARCHAR(36) NOT NULL,
    end_dot_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (start_dot_id) REFERENCES Dot(id),
    FOREIGN KEY (end_dot_id) REFERENCES Dot(id)
    );

CREATE TABLE IF NOT EXISTS RouteHistoryStep (
    id VARCHAR(36) PRIMARY KEY,
    route_history_id VARCHAR(36) NOT NULL,
    dot_id VARCHAR(36) NOT NULL,
    step_order INT NOT NULL,
    FOREIGN KEY (route_history_id) REFERENCES RouteHistory(id),
    FOREIGN KEY (dot_id) REFERENCES Dot(id),
    UNIQUE (route_history_id, step_order)
    );

