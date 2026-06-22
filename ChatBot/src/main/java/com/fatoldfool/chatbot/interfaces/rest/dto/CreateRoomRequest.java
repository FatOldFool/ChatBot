package com.fatoldfool.chatbot.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateRoomRequest {
    @NotBlank
    @Size(max = 50)
    private String roomName;

    public CreateRoomRequest() {}
    public CreateRoomRequest(String roomName) {
        this.roomName = roomName;
    }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
}