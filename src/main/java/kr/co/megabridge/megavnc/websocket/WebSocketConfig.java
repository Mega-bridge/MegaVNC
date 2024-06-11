package kr.co.megabridge.megavnc.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final RemotePcStatusHandler remotePcStatusHandler;

    public WebSocketConfig(RemotePcStatusHandler remotePcStatusHandler) {
        this.remotePcStatusHandler = remotePcStatusHandler;
    }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(remotePcStatusHandler, "/ws/status")
                //로컬
                //.setAllowedOrigins("https://localhost:8443");
                //배포시
                //.setAllowedOrigins("https://vnc.megabridge.com:8443");
                //테스트시
                .setAllowedOrigins("*");

    }


}
