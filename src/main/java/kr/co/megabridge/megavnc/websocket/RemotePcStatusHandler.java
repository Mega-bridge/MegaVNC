package kr.co.megabridge.megavnc.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class RemotePcStatusHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }


    public void sendStatusUpdate(Long pcId, String status, String registeredAt) {
        String statusUpdate = String.format("{\"id\": %d, \"status\": \"%s\", \"registeredAt\": \"%s\"}", pcId, status, registeredAt);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(statusUpdate));

                } catch (Exception e) {
                    log.error("sendStatusUpdate error {}", e.getMessage());
                }
            }
        }
    }
}
