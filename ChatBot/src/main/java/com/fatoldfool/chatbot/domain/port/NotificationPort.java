package com.fatoldfool.chatbot.domain.port;

import com.fatoldfool.chatbot.domain.model.Message;

public interface NotificationPort {
    void sendToRoom(Long roomId, Message message);
    void sendToRoom(Long roomId, Object payload);
    void sendToUser(String sessionId, Object payload);   // для приватных сообщений (например, ошибки)
}