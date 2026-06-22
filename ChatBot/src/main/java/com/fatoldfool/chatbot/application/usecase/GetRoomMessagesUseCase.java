package com.fatoldfool.chatbot.application.usecase;

import com.fatoldfool.chatbot.domain.model.Message;
import com.fatoldfool.chatbot.domain.port.LoggerPort;
import com.fatoldfool.chatbot.domain.port.MessageRepository;
import java.util.List;

public class GetRoomMessagesUseCase {
    private final MessageRepository messageRepository;
    private final LoggerPort logger;

    public GetRoomMessagesUseCase(MessageRepository messageRepository, LoggerPort logger) {
        this.messageRepository = messageRepository;
        this.logger = logger;
    }

    public List<Message> execute(Long roomId) {
        logger.info("Fetching all messages for room {}", roomId);
        return messageRepository.findByRoomId(roomId);
    }

    public List<Message> execute(Long roomId, int limit, int offset) {
        logger.info("Fetching messages for room {} with limit {} offset {}", roomId, limit, offset);
        return messageRepository.findByRoomId(roomId, limit, offset);
    }
}