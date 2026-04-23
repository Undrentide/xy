package domain.dao;

import domain.model.impl.Dot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for Dot persistence.
 * Provides basic operations for storing and retrieving dots.
 */

public interface DotDao {
    void save(Dot dot);

    Optional<Dot> findById(UUID id);

    List<Dot> findAll();

    /**
     * Returns total number of dots in the database.
     * Used to check if dots already exist in DB (for initial generation).
     */

    long count();
}