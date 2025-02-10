package io.github.vishalmysore.chatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class EasyQChatConfig implements WebSocketConfigurer {

    @Value("${allowedhosts}")
    private String allowedHosts;
    @Autowired
    private WebSocketHandshakeInterceptor handshakeInterceptor;
    @Autowired
    private EasyQScoreHandler easyQScoreHandler;



    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Register the first handler for the "/ws/chat" endpoint
        registry.addHandler(easyQScoreHandler, "/ws/score")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins(allowedHosts);

        // Register the second handler for the "/ws/notifications" endpoint
        registry.addHandler(new EasyQNotificationHandler(), "/ws/notifications")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins(allowedHosts);

        // Register another handler for a different WebSocket endpoint
        registry.addHandler(new EasyQAdminHandler(), "/ws/admin")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins(allowedHosts);
    }
}
