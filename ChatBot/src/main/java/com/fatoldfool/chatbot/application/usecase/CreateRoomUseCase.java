package com.fatoldfool.chatbot.application.usecase;

import com.fatoldfool.chatbot.application.command.CreateRoomCommand;
import com.fatoldfool.chatbot.application.result.CreateRoomResult;
import com.fatoldfool.chatbot.domain.model.ChatRoom;
import com.fatoldfool.chatbot.domain.port.ChatRoomRepository;
import com.fatoldfool.chatbot.domain.port.LoggerPort;

public class CreateRoomUseCase {
    private final ChatRoomRepository roomRepository;
    private final LoggerPort logger;

    public CreateRoomUseCase(ChatRoomRepository roomRepository, LoggerPort logger) {
        this.roomRepository = roomRepository;
        this.logger = logger;
    }

    public CreateRoomResult execute(CreateRoomCommand command) {
        logger.info("Creating room with name: {} by user: {}", command.roomName(), command.creatorUsername());
        try {
            var existing = roomRepository.findByName(command.roomName());
            if (existing.isPresent()) {
                logger.info("Room already exists: {}", command.roomName());
                return CreateRoomResult.failure("Room with name '" + command.roomName() + "' already exists");
            }

            ChatRoom room = ChatRoom.createNew(command.roomName());
            ChatRoom saved = roomRepository.save(room);
            logger.info("Room created with id: {}", saved.getId());
            return CreateRoomResult.success(saved);
        } catch (Exception e) {
            logger.error("Failed to create room: " + e.getMessage(), e);
            return CreateRoomResult.failure("Failed to create room: " + e.getMessage());
        }
    }
}