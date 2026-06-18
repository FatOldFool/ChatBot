package com.fatoldfool.chatbot.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private final Long id;
    private final Long roomId;
    private final String userId;
    private final String userName;
    private final String content;
    private final Instant createdAt;

    @JsonCreator
    public Message(@JsonProperty("id") Long id,
                   @JsonProperty("roomId") Long roomId,
                   @JsonProperty("userId") String userId,
                   @JsonProperty("userName") String userName,
                   @JsonProperty("content") String content,
                   @JsonProperty("createdAt") Instant createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static Message createNew(Long roomId, String userId, String userName, String content) {
        return new Message(null, roomId, userId, userName, content, Instant.now());
    }

    // getters
    public Long getId() { return id; }
    public Long getRoomId() { return roomId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id) &&
               Objects.equals(roomId, message.roomId) &&
               Objects.equals(userId, message.userId) &&
               Objects.equals(userName, message.userName) &&
               Objects.equals(content, message.content) &&
               Objects.equals(createdAt, message.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roomId, userId, userName, content, createdAt);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", roomId=" + roomId +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}