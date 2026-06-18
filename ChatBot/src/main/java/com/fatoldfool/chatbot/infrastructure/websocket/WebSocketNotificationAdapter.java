package com.fatoldfool.chatbot.infrastructure.websocket;

import com.fatoldfool.chatbot.domain.model.Message;
import com.fatoldfool.chatbot.domain.port.NotificationPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketNotificationAdapter implements NotificationPort {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendToRoom(Long roomId, Message message) {
        // Отправляем сообщение в STOMP-канал /topic/room/{roomId}
        messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    }
}