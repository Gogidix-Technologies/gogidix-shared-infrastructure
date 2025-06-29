package com.exalt.shared.ecommerce.admin.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of the EventPublisher interface that supports
 * multiple event destinations including WebSockets and message brokers.
 */
@Component
public class DefaultEventPublisher implements EventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(DefaultEventPublisher.class);
    
    private final List<EventSubscriber> subscribers = new ArrayList<>();
    private final Executor executor;
    private final AtomicBoolean running = new AtomicBoolean(true);
    
    @Autowired
    public DefaultEventPublisher() {
        this(Executors.newCachedThreadPool());
    }
    
    public DefaultEventPublisher(Executor executor) {
        this.executor = executor;
    }
    
    /**
     * Register a subscriber to receive published events.
     * 
     * @param subscriber The subscriber to register
     */
    public void subscribe(EventSubscriber subscriber) {
        synchronized (subscribers) {
            if (!subscribers.contains(subscriber)) {
                subscribers.add(subscriber);
            }
        }
    }
    
    /**
     * Unregister a subscriber.
     * 
     * @param subscriber The subscriber to unregister
     */
    public void unsubscribe(EventSubscriber subscriber) {
        synchronized (subscribers) {
            subscribers.remove(subscriber);
        }
    }
    
    @Override
    public boolean publish(Event event) {
        if (!running.get()) {
            logger.warn("Publisher is shutting down, event rejected: {}", event);
            return false;
        }
        
        if (event == null) {
            logger.warn("Attempted to publish null event");
            return false;
        }
        
        executor.execute(() -> deliverEvent(event, null));
        return true;
    }
    
    @Override
    public void publish(Event event, PublishCallback callback) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        if (!running.get()) {
            String errorMsg = "Publisher is shutting down";
            logger.warn("{}: {}", errorMsg, event);
            if (callback != null) {
                callback.onComplete(false, new IllegalStateException(errorMsg), event);
            }
            return;
        }
        
        executor.execute(() -> deliverEvent(event, callback));
    }
    
    @Override
    public boolean publishSync(Event event) {
        if (!running.get()) {
            logger.warn("Publisher is shutting down, event rejected: {}", event);
            return false;
        }
        
        if (event == null) {
            logger.warn("Attempted to publish null event");
            return false;
        }
        
        return deliverEvent(event, null);
    }
    
    /**
     * Shutdown the publisher and release resources.
     */
    public void shutdown() {
        running.set(false);
        // Additional cleanup if needed
    }
    
    private boolean deliverEvent(Event event, PublishCallback callback) {
        boolean success = true;
        Exception firstError = null;
        
        // Make a snapshot of subscribers to avoid ConcurrentModificationException
        EventSubscriber[] currentSubscribers;
        synchronized (subscribers) {
            currentSubscribers = subscribers.toArray(new EventSubscriber[0]);
        }
        
        // Deliver to all subscribers
        for (EventSubscriber subscriber : currentSubscribers) {
            try {
                if (!subscriber.supportsEvent(event)) {
                    continue;
                }
                
                if (!subscriber.onEvent(event)) {
                    success = false;
                    if (firstError == null) {
                        firstError = new Exception("Subscriber " + subscriber.getClass().getSimpleName() + " failed to process event");
                    }
                }
            } catch (Exception e) {
                success = false;
                if (firstError == null) {
                    firstError = e;
                }
                logger.error("Error delivering event to subscriber: " + subscriber.getClass().getSimpleName(), e);
            }
        }
        
        // Notify callback if provided
        if (callback != null) {
            try {
                callback.onComplete(success, firstError, event);
            } catch (Exception e) {
                logger.error("Error in publish callback", e);
            }
        }
        
        return success;
    }
}
