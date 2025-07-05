package com.gogidix.ecosystem.shared.admin.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all events in the admin framework.
 */
public abstract class Event {
    private final String eventId;
    private final String eventType;
    private final Instant timestamp;
    private final String source;
    
    protected Event(String eventType, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = Instant.now();
        this.source = source;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public String getSource() {
        return source;
    }
    
    @Override
    public String toString() {
        return String.format("Event{id=%s, type=%s, timestamp=%s, source=%s}",
                eventId, eventType, timestamp, source);
    }
}
