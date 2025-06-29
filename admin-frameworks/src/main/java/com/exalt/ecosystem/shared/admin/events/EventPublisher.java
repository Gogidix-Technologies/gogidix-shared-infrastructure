package com.exalt.ecosystem.shared.admin.events;

/**
 * Interface for publishing events within the admin framework.
 * Implementations can deliver events to various destinations (e.g., message brokers, WebSockets).
 */
public interface EventPublisher {
    
    /**
     * Publish an event asynchronously.
     * 
     * @param event The event to publish
     * @return true if the event was accepted for publishing, false otherwise
     */
    boolean publish(Event event);
    
    /**
     * Publish an event with a callback for handling the result.
     * 
     * @param event The event to publish
     * @param callback The callback to handle the publishing result
     */
    void publish(Event event, PublishCallback callback);
    
    /**
     * Publish an event synchronously.
     * 
     * @param event The event to publish
     * @return true if the event was published successfully, false otherwise
     */
    boolean publishSync(Event event);
    
    /**
     * Callback interface for handling asynchronous publishing results.
     */
    @FunctionalInterface
    interface PublishCallback {
        /**
         * Called when the publishing operation completes.
         * 
         * @param success true if the event was published successfully, false otherwise
         * @param error The error that occurred, or null if successful
         * @param event The event that was published
         */
        void onComplete(boolean success, Throwable error, Event event);
    }
}
