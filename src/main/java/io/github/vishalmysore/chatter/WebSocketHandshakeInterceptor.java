package io.github.vishalmysore.chatter;

import io.github.vishalmysore.security.JwtUtil;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {



    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // Retrieve the token from the query parameters or headers
        String token = request.getURI().getQuery().split("=")[1]; // Assuming the token is passed as a query parameter like ?token=your_jwt_token
        String userId = null;
        if (token != null) {
            JwtUtil jwtUtil = new JwtUtil();
            try {
                userId = jwtUtil.getUserId(token);
            }catch (Exception e){
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
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // This method is called after the handshake is completed.
        // You can perform any post-handshake tasks here.
    }
}
