package com.fatoldfool.chatbot.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fatoldfool.chatbot.domain.model.Message;
import com.fatoldfool.chatbot.domain.port.MessageRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class FileMessageRepository implements MessageRepository {
    private final Map<Long, Message> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ObjectMapper objectMapper;
    private final String filePath;

    public FileMessageRepository(@Value("${chatbot.storage.messages-file-path:./chatbot-messages.json}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) return;
        try {
            List<Message> messages = objectMapper.readValue(file, new TypeReference<>() {});
            synchronized (this) {
                store.clear();
                long maxId = 0;
                for (Message msg : messages) {
                    store.put(msg.getId(), msg);
                    if (msg.getId() > maxId) maxId = msg.getId();
                }
                idGenerator.set(maxId + 1);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load messages from file", e);
        }
    }

    private void saveToFile() {
        try {
            List<Message> list = new ArrayList<>(store.values());
            objectMapper.writeValue(new File(filePath), list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save messages to file", e);
        }
    }

    @Override
    public synchronized Message save(Message message) {
        if (message.getId() == null) {
            Message newMsg = new Message(
                    idGenerator.getAndIncrement(),
                    message.getRoomId(),
                    message.getUserId(),
                    message.getUserName(),
                    message.getContent(),
                    message.getCreatedAt()
            );
            store.put(newMsg.getId(), newMsg);
            saveToFile();
            return newMsg;
        } else {
            store.put(message.getId(), message);
            saveToFile();
            return message;
        }
    }

    @Override
    public List<Message> findByRoomId(Long roomId) {
        return store.values().stream()
                .filter(msg -> msg.getRoomId().equals(roomId))
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void deleteAll() {
        store.clear();
        idGenerator.set(1);
        saveToFile();
    }
}