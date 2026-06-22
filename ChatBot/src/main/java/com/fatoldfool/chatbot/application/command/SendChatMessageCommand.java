package com.fatoldfool.chatbot.application.command;

import com.fatoldfool.chatbot.domain.exception.InvalidMessageException;

public record SendChatMessageCommand(Long roomId, String username, String content) {
    public SendChatMessageCommand {
        if (roomId == null) {
            throw new InvalidMessageException("Room ID cannot be null");
        }
        if (username == null || username.isBlank()) {
            throw new InvalidMessageException("Username cannot be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new InvalidMessageException("Message content cannot be empty");
        }
        if (content.length() > 1000) {
            throw new InvalidMessageException("Message content cannot exceed 1000 characters");
        }
    }
}