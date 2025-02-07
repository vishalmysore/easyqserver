package io.github.vishalmysore.chatter;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;

public class EasyQAdminHandler extends AbstractWebSocketHandler {
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            session.sendMessage(new TextMessage("Hello, client!"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
