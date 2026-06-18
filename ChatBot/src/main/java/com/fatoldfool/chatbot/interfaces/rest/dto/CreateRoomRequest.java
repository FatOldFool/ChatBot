package com.fatoldfool.chatbot.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateRoomRequest {
    @NotBlank
    private String roomName;
    @NotBlank
    private String creatorSessionId;

    // constructors, getters, setters
    public CreateRoomRequest() {}
    public CreateRoomRequest(String roomName, String creatorSessionId) {
        this.roomName = roomName;
        this.creatorSessionId = creatorSessionId;
    }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getCreatorSessionId() { return creatorSessionId; }
    public void setCreatorSessionId(String creatorSessionId) { this.creatorSessionId = creatorSessionId; }
}