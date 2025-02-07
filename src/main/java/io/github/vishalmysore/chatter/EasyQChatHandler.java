package io.github.vishalmysore.chatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Slf4j

public class EasyQChatHandler extends TextWebSocketHandler {
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
        } else {
            log.info("Unauthorized connection attempt!");
            session.close(); // Optionally close the session if token or userId is missing
        }
    }

}
