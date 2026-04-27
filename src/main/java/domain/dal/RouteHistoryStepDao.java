package domain.dal;

import domain.model.impl.RouteHistoryStep;

import java.util.List;
import java.util.UUID;

/**
 * DAO for RouteHistoryStep persistence.
 * Stores route steps and allows reading steps of a route in correct order.
 */
public interface RouteHistoryStepDao extends Dao<RouteHistoryStep> {
    List<RouteHistoryStep> findRouteHistoryStepListById(UUID routeHistoryId);
}