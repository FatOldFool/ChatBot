package com.fatoldfool.chatbot.domain.port;

import com.fatoldfool.chatbot.domain.model.Message;
import java.util.List;

public interface MessageRepository {
    Message save(Message message);
    List<Message> findByRoomId(Long roomId);
    List<Message> findByRoomId(Long roomId, int limit, int offset);  // новый
    void deleteAll();
}