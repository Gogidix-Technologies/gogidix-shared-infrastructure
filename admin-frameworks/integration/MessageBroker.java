package com.exalt.shared.ecommerce.admin.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.microsocial.ecommerce.admin.util.Logger;

/**
 * Message broker for publishing and subscribing to messages.
 * This class provides functionality for publishing and subscribing to messages
 * in the admin framework.
 */
public class MessageBroker {
    
    private static final Logger logger = Logger.getLogger(MessageBroker.class);
    
    private final Map<String, List<Subscriber>> subscribers;
    
    /**
     * Default constructor
     */
    public MessageBroker() {
        this.subscribers = new HashMap<>();
    }
    
    /**
     * Publish a message to a topic
     * 
     * @param topic The topic to publish to
     * @param message The message to publish
     */
    public void publish(String topic, Object message) {
        logger.debug("Publishing message to topic: " + topic);
        
        if (!subscribers.containsKey(topic)) {
            logger.debug("No subscribers for topic: " + topic);
            return;
        }
        
        subscribers.get(topic).forEach(subscriber -> {
            try {
                subscriber.onMessage(message);
            } catch (Exception e) {
                logger.error("Error in subscriber: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Publish a message to a topic asynchronously
     * 
     * @param topic The topic to publish to
     * @param message The message to publish
     * @return A CompletableFuture for when all subscribers have processed the message
     */
    public CompletableFuture<Void> publishAsync(String topic, Object message) {
        logger.debug("Publishing message asynchronously to topic: " + topic);
        
        if (!subscribers.containsKey(topic)) {
            logger.debug("No subscribers for topic: " + topic);
            return CompletableFuture.completedFuture(null);
        }
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        subscribers.get(topic).forEach(subscriber -> {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    subscriber.onMessage(message);
                } catch (Exception e) {
                    logger.error("Error in subscriber: " + e.getMessage(), e);
                }
            });
            
            futures.add(future);
        });
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    /**
     * Subscribe to a topic
     * 
     * @param topic The topic to subscribe to
     * @param callback The callback to invoke when a message is published to the topic
     * @return The subscriber ID
     */
    public UUID subscribe(String topic, Consumer<Object> callback) {
        logger.debug("Subscribing to topic: " + topic);
        
        Subscriber subscriber = new Subscriber(UUID.randomUUID(), callback);
        
        subscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(subscriber);
        
        return subscriber.getId();
    }
    
    /**
     * Unsubscribe from a topic
     * 
     * @param topic The topic to unsubscribe from
     * @param subscriberId The subscriber ID
     * @return true if the subscriber was unsubscribed, false otherwise
     */
    public boolean unsubscribe(String topic, UUID subscriberId) {
        logger.debug("Unsubscribing from topic: " + topic);
        
        if (!subscribers.containsKey(topic)) {
            return false;
        }
        
        List<Subscriber> topicSubscribers = subscribers.get(topic);
        boolean removed = topicSubscribers.removeIf(subscriber -> subscriber.getId().equals(subscriberId));
        
        if (topicSubscribers.isEmpty()) {
            subscribers.remove(topic);
        }
        
        return removed;
    }
    
    /**
     * Subscriber class
     */
    private static class Subscriber {
        private final UUID id;
        private final Consumer<Object> callback;
        
        /**
         * Constructor with ID and callback
         * 
         * @param id The subscriber ID
         * @param callback The callback to invoke when a message is published
         */
        public Subscriber(UUID id, Consumer<Object> callback) {
            this.id = id;
            this.callback = callback;
        }
        
        /**
         * Get the subscriber ID
         * 
         * @return The subscriber ID
         */
        public UUID getId() {
            return id;
        }
        
        /**
         * Handle a message
         * 
         * @param message The message to handle
         */
        public void onMessage(Object message) {
            callback.accept(message);
        }
    }
}
