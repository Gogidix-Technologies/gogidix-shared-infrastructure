package com.gogidix.shared.ecommerce.admin.websocket;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsocial.ecommerce.admin.events.Event;

@ExtendWith(MockitoExtension.class)
public class WebSocketEventPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private WebSocketEventPublisher webSocketEventPublisher;
    
    private TestEvent testEvent;
    
    @BeforeEach
    void setUp() {
        testEvent = new TestEvent("test.source");
    }
    
    @Test
    void testPublishEvent() throws JsonProcessingException {
        // Arrange
        String eventJson = "{\"eventId\":\"test-id\"}";
        when(objectMapper.writeValueAsString(any(WebSocketEventPublisher.WebSocketEvent.class)))
            .thenReturn(eventJson);
        
        // Act
        webSocketEventPublisher.onEvent(testEvent);
        
        // Assert
        verify(messagingTemplate, times(1))
            .convertAndSend(eq("/topic/events"), eq(eventJson));
    }
    
    @Test
    void testPublishEventWithTargetUser() throws JsonProcessingException {
        // Arrange
        String userId = "user123";
        String eventJson = "{\"eventId\":\"test-id\"}";
        testEvent.setTargetUserId(userId);
        
        when(objectMapper.writeValueAsString(any(WebSocketEventPublisher.WebSocketEvent.class)))
            .thenReturn(eventJson);
        
        // Act
        webSocketEventPublisher.onEvent(testEvent);
        
        // Assert
        verify(messagingTemplate, times(1))
            .convertAndSend(eq("/topic/events"), eq(eventJson));
        verify(messagingTemplate, times(1))
            .convertAndSendToUser(
                eq(userId), 
                eq("/queue/events-test-event"), 
                eq(eventJson));
    }
    
    @Test
    void testPublishEventWithJsonError() throws JsonProcessingException {
        // Arrange
        when(objectMapper.writeValueAsString(any(WebSocketEventPublisher.WebSocketEvent.class)))
            .thenThrow(new JsonProcessingException("Test error") {});
        
        // Act
        boolean result = webSocketEventPublisher.onEvent(testEvent);
        
        // Assert
        assertFalse(result, "Should return false on JSON processing error");
        verify(messagingTemplate, never())
            .convertAndSend(anyString(), any());
    }
    
    private static class TestEvent extends Event {
        private String targetUserId;
        
        public TestEvent(String source) {
            super("test.event", source);
        }
        
        @Override
        public String getEventType() {
            return "test.event";
        }
        
        public void setTargetUserId(String targetUserId) {
            this.targetUserId = targetUserId;
        }
        
        @Override
        public String getTargetUserId() {
            return targetUserId;
        }
    }
}
