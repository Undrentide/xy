package domain.dao;

import domain.model.impl.RouteHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for RouteHistory persistence.
 * Stores route history records and allows reading the latest routes.
 */

public interface RouteHistoryDao {
    void save(RouteHistory routeHistory);

    Optional<RouteHistory> findById(UUID id);

    List<RouteHistory> findLastRoutes(int limit);

    long count();
}