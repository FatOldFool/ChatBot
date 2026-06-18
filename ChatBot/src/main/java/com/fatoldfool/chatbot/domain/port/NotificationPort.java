package com.fatoldfool.chatbot.domain.port;

import com.fatoldfool.chatbot.domain.model.Message;

public interface NotificationPort {
    void sendToRoom(Long roomId, Message message);
}