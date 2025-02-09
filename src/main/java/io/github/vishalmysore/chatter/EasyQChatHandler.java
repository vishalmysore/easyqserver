package io.github.vishalmysore.chatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EasyQChatHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String userId = (String) session.getAttributes().get("userId");
            session.sendMessage(new TextMessage("Hello, "+userId));
        } catch (IOException e) {
            log.error(String.valueOf(e));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Retrieve the token and userId from the session attributes
        String token = (String) session.getAttributes().get("token");
        String userId = (String) session.getAttributes().get("userId");

        // Perform actions using token and userId
        if (token != null && userId != null) {
            log.info("Connection established with user: " + userId);
            userSessions.put(userId, session);
            log.info("Session stored for user: " + userId);
        } else {
            log.info("Unauthorized connection attempt!");
            session.close(); // Optionally close the session if token or userId is missing
        }
    }
    public WebSocketSession getSessionByUserId(String userId) {
        return userSessions.get(userId);
    }

    // Method to send a message to a specific user
    public void sendMessageToUser(String userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.info("Sent message to user: " + userId);
            } catch (IOException e) {
                log.error("Error sending message to user " + userId + ": " + e.getMessage());
            }
        } else {
            log.warn("No open session found for user: " + userId);
        }
    }

    // Method to remove the session once the connection is closed
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            userSessions.remove(userId);
            log.info("Session removed for user: " + userId);
        }
    }
}
