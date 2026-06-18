package com.fatoldfool.chatbot.interfaces.rest.dto;

import com.fatoldfool.chatbot.domain.model.ChatRoom;

public class RoomResponse {
    private Long id;
    private String name;
    private String createdAt;

    public RoomResponse(Long id, String name, String createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static RoomResponse fromDomain(ChatRoom room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCreatedAt().toString());
    }

    // getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCreatedAt() { return createdAt; }
}