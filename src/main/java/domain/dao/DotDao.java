package domain.dao;

import domain.model.impl.Dot;

import java.util.List;

/**
 * DAO for Dot persistence.
 * Provides basic operations for storing and retrieving dots.
 */

public interface DotDao extends Dao<Dot> {
    List<Dot> findAll();
}