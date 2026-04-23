package domain.dao;

import domain.model.impl.RouteHistoryStep;

import java.util.List;
import java.util.UUID;

/**
 * DAO for RouteHistoryStep persistence.
 * Stores route steps and allows reading steps of a route in correct order.
 */

public interface RouteHistoryStepDao {
    void save(RouteHistoryStep routeHistoryStep);

    List<RouteHistoryStep> findByRouteHistoryId(UUID routeHistoryId);

    long count();
}