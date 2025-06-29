package com.exalt.shared.ecommerce.admin.dal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of the Repository interface.
 * This class provides a JPA-based implementation of the Repository interface.
 * 
 * @param <T> The entity type
 */
public class JpaRepository<T> implements Repository<T> {
    
    private final Class<T> entityClass;
    private final Map<UUID, T> entities;
    
    /**
     * Constructor with entity class
     * 
     * @param entityClass The entity class
     */
    public JpaRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.entities = new HashMap<>();
    }
    
    @Override
    public T save(T entity) {
        // In a real implementation, this would use JPA's EntityManager
        // to persist the entity
        
        // For now, just simulate saving to the map
        try {
            UUID id = (UUID) entityClass.getMethod("getId").invoke(entity);
            entities.put(id, entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Error saving entity: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<T> findById(UUID id) {
        // In a real implementation, this would use JPA's EntityManager
        // to find the entity by ID
        
        // For now, just get from the map
        return Optional.ofNullable(entities.get(id));
    }
    
    @Override
    public List<T> findAll() {
        // In a real implementation, this would use JPA's EntityManager
        // to find all entities
        
        // For now, just return all entities from the map
        return new ArrayList<>(entities.values());
    }
    
    @Override
    public void delete(T entity) {
        // In a real implementation, this would use JPA's EntityManager
        // to delete the entity
        
        // For now, just remove from the map
        try {
            UUID id = (UUID) entityClass.getMethod("getId").invoke(entity);
            entities.remove(id);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting entity: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean deleteById(UUID id) {
        // In a real implementation, this would use JPA's EntityManager
        // to delete the entity by ID
        
        // For now, just remove from the map
        return entities.remove(id) != null;
    }
    
    @Override
    public long count() {
        // In a real implementation, this would use JPA's EntityManager
        // to count all entities
        
        // For now, just return the size of the map
        return entities.size();
    }
}
