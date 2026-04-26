package domain.model.impl;

import domain.model.Entity;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Dot extends Entity {
    private final String name;
    private final Double x;
    private final Double y;

    public Dot(String name, Double x, Double y) {
        super();
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public Dot(UUID id, String name, Double x, Double y) {
        super(id);
        this.name = name;
        this.x = x;
        this.y = y;
    }
}