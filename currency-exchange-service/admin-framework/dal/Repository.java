package com.exalt.shared.ecommerce.admin.dal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Generic repository interface for data access.
 * This interface defines the common operations for data access.
 * 
 * @param <T> The entity type
 */
public interface Repository<T> {
    
    /**
     * Save an entity
     * 
     * @param entity The entity to save
     * @return The saved entity
     */
    T save(T entity);
    
    /**
     * Find an entity by ID
     * 
     * @param id The ID of the entity
     * @return An Optional containing the entity if found, empty otherwise
     */
    Optional<T> findById(UUID id);
    
    /**
     * Find all entities
     * 
     * @return A list of all entities
     */
    List<T> findAll();
    
    /**
     * Delete an entity
     * 
     * @param entity The entity to delete
     */
    void delete(T entity);
    
    /**
     * Delete an entity by ID
     * 
     * @param id The ID of the entity to delete
     * @return true if the entity was deleted, false otherwise
     */
    boolean deleteById(UUID id);
    
    /**
     * Count all entities
     * 
     * @return The number of entities
     */
    long count();
}
