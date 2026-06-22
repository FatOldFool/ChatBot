package com.fatoldfool.chatbot.infrastructure.persistence;

import com.fatoldfool.chatbot.domain.model.Message;
import com.fatoldfool.chatbot.domain.port.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FileMessageRepository extends AbstractFileRepository<Message> implements MessageRepository {

    public FileMessageRepository(@Value("${chatbot.storage.messages-file-path:./chatbot-messages.json}") String filePath) {
        super(filePath);
    }

    @Override
    public Message save(Message message) {
        return super.save(message);
    }

    @Override
    public List<Message> findByRoomId(Long roomId) {
        return store.values().stream()
                .filter(msg -> msg.getRoomId().equals(roomId))
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByRoomId(Long roomId, int limit, int offset) {
        List<Message> all = store.values().stream()
                .filter(msg -> msg.getRoomId().equals(roomId))
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .collect(Collectors.toList());
        if (offset >= all.size()) return List.of();
        int toIndex = Math.min(offset + limit, all.size());
        return all.subList(offset, toIndex);
    }

    @Override
    public void deleteAll() {
        super.deleteAll();
    }

    @Override
    protected Long extractId(Message msg) { return msg.getId(); }

    @Override
    protected Message createWithId(Message msg, Long id) {
        return new Message(id, msg.getRoomId(), msg.getUserId(), msg.getUserName(), msg.getContent(), msg.getCreatedAt());
    }
}