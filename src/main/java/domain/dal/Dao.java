package domain.dal;

import domain.model.Entity;

import java.util.Optional;
import java.util.UUID;

/**
 * Base DAO interface
 * provides general CR operations
 */
public interface Dao<T extends Entity> {
    void save(T entity);

    Optional<T> findById(UUID id);
}