package com.fatoldfool.chatbot.infrastructure.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fatoldfool.chatbot.domain.model.ChatRoom;
import com.fatoldfool.chatbot.domain.port.ChatRoomRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class FileChatRoomRepository implements ChatRoomRepository {
    private final Map<Long, ChatRoom> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ObjectMapper objectMapper;
    private final String filePath;

    public FileChatRoomRepository(@Value("${chatbot.storage.rooms-file-path:./chatbot-rooms.json}") String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) return;
        try {
            List<ChatRoom> rooms = objectMapper.readValue(file, new TypeReference<>() {});
            synchronized (this) {
                store.clear();
                long maxId = 0;
                for (ChatRoom room : rooms) {
                    store.put(room.getId(), room);
                    if (room.getId() > maxId) maxId = room.getId();
                }
                idGenerator.set(maxId + 1);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load chat rooms from file", e);
        }
    }

    private void saveToFile() {
        try {
            List<ChatRoom> list = new ArrayList<>(store.values());
            objectMapper.writeValue(new File(filePath), list);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chat rooms to file", e);
        }
    }

    @Override
    public synchronized ChatRoom save(ChatRoom room) {
        if (room.getId() == null) {
            ChatRoom newRoom = new ChatRoom(idGenerator.getAndIncrement(), room.getName(), room.getCreatedAt());
            store.put(newRoom.getId(), newRoom);
            saveToFile();
            return newRoom;
        } else {
            store.put(room.getId(), room);
            saveToFile();
            return room;
        }
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
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized void deleteAll() {
        store.clear();
        idGenerator.set(1);
        saveToFile();
    }
}