package com.exalt.ecosystem.shared.admin.integration;

import java.util.List;

/**
 * Service interface for synchronizing data between the admin framework
 * and the centralized dashboard.
 */
public interface DataSynchronizationService {
    
    /**
     * Synchronizes all data with the centralized dashboard
     * @return true if synchronization was successful, false otherwise
     */
    boolean syncAll();
    
    /**
     * Synchronizes a specific entity with the centralized dashboard
     * @param entityType The type of entity to synchronize
     * @param entityId The ID of the entity to synchronize
     * @return true if synchronization was successful, false otherwise
     */
    boolean syncEntity(String entityType, String entityId);
    
    /**
     * Gets the status of the last synchronization
     * @return A status message
     */
    String getLastSyncStatus();
    
    /**
     * Gets the timestamp of the last successful synchronization
     * @return The timestamp in milliseconds since epoch, or -1 if never synced
     */
    long getLastSyncTimestamp();
    
    /**
     * Gets a list of entities that failed to sync
     * @return A list of entity IDs that failed to sync
     */
    List<String> getFailedSyncs();
    
    /**
     * Retries failed synchronizations
     * @return The number of retry attempts made
     */
    int retryFailedSyncs();
}
