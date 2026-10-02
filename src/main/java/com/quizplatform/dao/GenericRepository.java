package com.quizplatform.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface demonstrating generic programming in DAO architecture.
 *
 * @param <T>  The entity domain model type
 * @param <ID> The primary key / identifier type
 */
public interface GenericRepository<T, ID> {

    /**
     * Finds an entity by its identifier.
     * Demonstrates generic method return type and Optional<T>.
     *
     * @param id The entity ID
     * @return Optional containing the entity if found, or empty Optional
     */
    Optional<T> findById(ID id);

    /**
     * Retrieves all entities of type T.
     * Demonstrates generic collection List<T>.
     *
     * @return List of all entities
     */
    List<T> findAll();

    /**
     * Deletes an entity by its identifier.
     *
     * @param id The entity ID to delete
     * @return true if deleted, false otherwise
     */
    boolean deleteById(ID id);
}
