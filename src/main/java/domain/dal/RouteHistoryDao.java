package domain.dal;

import domain.model.impl.RouteHistory;

import java.util.List;

/**
 * DAO for RouteHistory persistence.
 * Stores route history records and allows reading the latest routes.
 */
public interface RouteHistoryDao extends Dao<RouteHistory> {
    List<RouteHistory> findLastRoutes(int limit);
}