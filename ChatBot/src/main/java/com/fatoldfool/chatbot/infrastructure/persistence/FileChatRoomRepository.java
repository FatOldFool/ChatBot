package com.fatoldfool.chatbot.infrastructure.persistence;

import com.fatoldfool.chatbot.domain.model.ChatRoom;
import com.fatoldfool.chatbot.domain.port.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class FileChatRoomRepository extends AbstractFileRepository<ChatRoom> implements ChatRoomRepository {

    public FileChatRoomRepository(@Value("${chatbot.storage.rooms-file-path:./chatbot-rooms.json}") String filePath) {
        super(filePath);
    }

    @Override
    public ChatRoom save(ChatRoom room) {
        return super.save(room);
    }

    @Override
    public Optional<ChatRoom> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<ChatRoom> findByName(String name) {
        return store.values().stream()
                .filter(room -> room.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<ChatRoom> findAll() {
        return getAll();
    }

    @Override
    public void deleteAll() {
        super.deleteAll();
    }

    @Override
    protected Long extractId(ChatRoom room) {
        return room.getId();
    }

    @Override
    protected ChatRoom createWithId(ChatRoom room, Long id) {
        return new ChatRoom(id, room.getName(), room.getCreatedAt());
    }
}