package com.fatoldfool.chatbot.application.result;

import com.fatoldfool.chatbot.domain.model.ChatRoom;

public record CreateRoomResult(boolean success, ChatRoom room, String errorMessage) {
    public static CreateRoomResult success(ChatRoom room) {
        return new CreateRoomResult(true, room, null);
    }
    public static CreateRoomResult failure(String errorMessage) {
        return new CreateRoomResult(false, null, errorMessage);
    }
}