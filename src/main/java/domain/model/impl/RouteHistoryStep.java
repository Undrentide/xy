package domain.model.impl;

import domain.model.Entity;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RouteHistoryStep extends Entity {
    private final UUID routeHistoryId;
    private final UUID dotId;
    private final Integer stepOrder;

    public RouteHistoryStep(UUID routeHistoryId, UUID dotId, Integer stepOrder) {
        super();
        this.routeHistoryId = routeHistoryId;
        this.dotId = dotId;
        this.stepOrder = stepOrder;
    }

    public RouteHistoryStep(UUID id, UUID routeHistoryId, UUID dotId, Integer stepOrder) {
        super(id);
        this.routeHistoryId = routeHistoryId;
        this.dotId = dotId;
        this.stepOrder = stepOrder;
    }
}