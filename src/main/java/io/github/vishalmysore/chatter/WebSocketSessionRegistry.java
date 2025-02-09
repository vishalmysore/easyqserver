package io.github.vishalmysore.chatter;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionRegistry {

    // HashMap to store WebSocket sessions, keyed by userId
    private final ConcurrentHashMap<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public void addSession(String userId, WebSocketSession session) {
        sessionMap.put(userId, session);
    }

    public WebSocketSession getSession(String userId) {
        return sessionMap.get(userId);
    }

    public void removeSession(String userId) {
        sessionMap.remove(userId);
    }

    public boolean hasSession(String userId) {
        return sessionMap.containsKey(userId);
    }
}

