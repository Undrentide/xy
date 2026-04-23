package domain.dao;

import domain.model.Entity;

import java.util.Optional;
import java.util.UUID;

public interface Dao<T extends Entity> {
    void save(T entity);

    Optional<T> findById(UUID id);
}
