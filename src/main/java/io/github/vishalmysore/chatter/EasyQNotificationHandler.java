package io.github.vishalmysore.chatter;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public class EasyQNotificationHandler extends AbstractEasyQWSHandler {
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            session.sendMessage(new TextMessage("Hello, client!"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
