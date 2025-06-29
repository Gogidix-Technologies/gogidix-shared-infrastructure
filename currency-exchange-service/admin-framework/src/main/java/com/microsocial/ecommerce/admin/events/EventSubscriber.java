package com.exalt.shared.ecommerce.admin.events;

/**
 * Interface for components that want to subscribe to events from the EventPublisher.
 */
public interface EventSubscriber {
    
    /**
     * Check if this subscriber supports the given event.
     * 
     * @param event The event to check
     * @return true if this subscriber can handle the event, false otherwise
     */
    boolean supportsEvent(Event event);
    
    /**
     * Process the given event.
     * 
     * @param event The event to process
     * @return true if the event was processed successfully, false otherwise
     */
    boolean onEvent(Event event);
    
    /**
     * Get the subscriber's name for logging and identification.
     * 
     * @return The subscriber's name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
