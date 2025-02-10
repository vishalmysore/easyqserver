package io.github.vishalmysore.chatter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@Component
public class EasyQScoreHandler extends AbstractEasyQWSHandler {



    // Method to send a message to a specific user
    public void sendScoreToUser(String userId, Double score) {
        WebSocketSession session = getUserSessions().get(userId);
        if (session != null && session.isOpen()) {
            try {
                String jsonResponse = "{ \"action\": \"scoreUpdated\", \"newScore\": "+score+" }";
                session.sendMessage(new TextMessage(jsonResponse));
                log.info("Sent message to user: " + userId);
            } catch (IOException e) {
                log.error("Error sending message to user " + userId + ": " + e.getMessage());
            }
        } else {
            log.warn("No open session found for user: " + userId);
        }
    }
}
