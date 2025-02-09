package io.github.vishalmysore.chatter;

import io.github.vishalmysore.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;


    private final WebSocketSessionRegistry sessionRegistry;

    public WebSocketHandshakeInterceptor(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // Retrieve the token from the query parameters or headers
        String token = request.getURI().getQuery().split("=")[1]; // Assuming the token is passed as a query parameter like ?token=your_jwt_token
        String userId = null;
        if (token != null) {

            try {
                userId = jwtUtil.getUserId(token);
            }catch (Exception e){
                log.warn("Error validating token: " + e.getMessage());
                response.setStatusCode(HttpStatusCode.valueOf(401)); // Unauthorized status code
                return false;
            }

        } else {
            response.setStatusCode(HttpStatusCode.valueOf(401)); // Forbidden status code
            return false;
        }

        // Add the token to the WebSocket session attributes for later reference
        attributes.put("token", token);
        attributes.put("userId", userId);
        return true; // Proceed with the WebSocket handshake
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
        // Retrieve the WebSocket session and userId from the handshake attributes


    }

}
