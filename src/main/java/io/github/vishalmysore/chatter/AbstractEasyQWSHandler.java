package io.github.vishalmysore.chatter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public abstract class AbstractEasyQWSHandler extends AbstractWebSocketHandler {
    @Getter
    protected final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
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
