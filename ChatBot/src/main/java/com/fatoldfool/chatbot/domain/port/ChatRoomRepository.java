package com.fatoldfool.chatbot.domain.port;

import com.fatoldfool.chatbot.domain.model.ChatRoom;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {
    ChatRoom save(ChatRoom room);
    Optional<ChatRoom> findById(Long id);
    Optional<ChatRoom> findByName(String name);
    List<ChatRoom> findAll();
    void deleteAll();
}