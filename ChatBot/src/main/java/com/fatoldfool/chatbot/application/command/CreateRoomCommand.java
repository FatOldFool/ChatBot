package com.fatoldfool.chatbot.application.command;

import com.fatoldfool.chatbot.domain.exception.InvalidMessageException;

public record CreateRoomCommand(String roomName, String creatorUsername) {
    public CreateRoomCommand {
        if (roomName == null || roomName.isBlank()) {
            throw new InvalidMessageException("Room name cannot be null or blank");
        }
        if (creatorUsername == null || creatorUsername.isBlank()) {
            throw new InvalidMessageException("Creator username cannot be null or blank");
        }
        if (roomName.length() > 50) {
            throw new InvalidMessageException("Room name cannot exceed 50 characters");
        }
    }
}