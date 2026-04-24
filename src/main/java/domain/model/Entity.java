package domain.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class Entity {
    private final UUID id;

    protected Entity() {
        this.id = UUID.randomUUID();
    }

    protected Entity(UUID id) {
        this.id = id;
    }
}