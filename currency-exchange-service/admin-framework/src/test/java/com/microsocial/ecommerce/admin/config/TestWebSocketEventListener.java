package com.exalt.shared.ecommerce.admin.config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

/**
 * Test WebSocket event listener for tracking WebSocket events during testing.
 */
@Component
@Scope("prototype")
public class TestWebSocketEventListener extends StompSubProtocolErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(TestWebSocketEventListener.class);
    
    private final List<Message<?>> messages = new ArrayList<>();
    private final List<Throwable> exceptions = new ArrayList<>();
    private CountDownLatch messageLatch = new CountDownLatch(1);
    private int expectedMessageCount = 1;
    
    @Override
    public Message<byte[]> handleClientMessageProcessingError(
            Message<byte[]> clientMessage, Throwable ex) {
        logger.error("WebSocket error: {}", ex.getMessage(), ex);
        exceptions.add(ex);
        messageLatch.countDown();
        return super.handleClientMessageProcessingError(clientMessage, ex);
    }
    
    @Override
    public Message<byte[]> handleErrorMessageToClient(Message<byte[]> errorMessage) {
        logger.warn("WebSocket error message: {}", new String(errorMessage.getPayload()));
        messages.add(errorMessage);
        checkAndCountDown();
        return super.handleErrorMessageToClient(errorMessage);
    }
    
    public void expectMessageCount(int count) {
        this.expectedMessageCount = count;
        this.messageLatch = new CountDownLatch(count);
    }
    
    public boolean awaitMessages(long timeout, TimeUnit unit) throws InterruptedException {
        return messageLatch.await(timeout, unit);
    }
    
    public List<Message<?>> getMessages() {
        return new ArrayList<>(messages);
    }
    
    public List<Throwable> getExceptions() {
        return new ArrayList<>(exceptions);
    }
    
    public void reset() {
        messages.clear();
        exceptions.clear();
        messageLatch = new CountDownLatch(expectedMessageCount);
    }
    
    private void checkAndCountDown() {
        if (messages.size() >= expectedMessageCount) {
            while (messageLatch.getCount() > 0) {
                messageLatch.countDown();
            }
        }
    }
    
    public static Message<byte[]> createTestMessage(StompCommand command) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId("test-session-" + System.currentTimeMillis());
        accessor.setDestination("/topic/test");
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
