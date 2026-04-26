package domain.model.impl;

import domain.model.Entity;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class RouteHistory extends Entity {
    private final UUID startDotId;
    private final UUID endDotId;
    private final Instant createdAt;

    public RouteHistory(UUID startDotId, UUID endDotId) {
        super();
        this.startDotId = startDotId;
        this.endDotId = endDotId;
        this.createdAt = Instant.now();
    }

    public RouteHistory(UUID id, UUID startDotId, UUID endDotId, Instant createdAt) {
        super(id);
        this.startDotId = startDotId;
        this.endDotId = endDotId;
        this.createdAt = createdAt;
    }
}