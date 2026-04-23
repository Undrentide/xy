package domain.model.impl;

import domain.model.Entity;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class RouteHistory extends Entity {
    private final UUID startDotId;
    private final UUID endDotId;
    private final LocalDateTime createdAt;

    public RouteHistory(UUID startDotId, UUID endDotId, LocalDateTime createdAt) {
        super();
        this.startDotId = startDotId;
        this.endDotId = endDotId;
        this.createdAt = createdAt;
    }

    public RouteHistory(UUID id, UUID startDotId, UUID endDotId, LocalDateTime createdAt) {
        super(id);
        this.startDotId = startDotId;
        this.endDotId = endDotId;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format(
                "%s -> %s (%s)",
                startDotId,
                endDotId,
                createdAt
        );
    }
}
