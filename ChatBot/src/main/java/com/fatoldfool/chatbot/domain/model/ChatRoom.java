package com.fatoldfool.chatbot.domain.model;

import java.time.Instant;
import java.util.Objects;

public class ChatRoom {
    private final Long id;
    private final String name;
    private final Instant createdAt;

    public ChatRoom(Long id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static ChatRoom createNew(String name) {
        return new ChatRoom(null, name, Instant.now());
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return Objects.equals(id, chatRoom.id) &&
               Objects.equals(name, chatRoom.name) &&
               Objects.equals(createdAt, chatRoom.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, createdAt);
    }
}