package com.fatoldfool.chatbot.application.usecase;

import com.fatoldfool.chatbot.application.command.SendChatMessageCommand;
import com.fatoldfool.chatbot.domain.model.Message;
import com.fatoldfool.chatbot.domain.port.LoggerPort;
import com.fatoldfool.chatbot.domain.port.MessageRepository;
import com.fatoldfool.chatbot.domain.port.NotificationPort;
import com.fatoldfool.chatbot.domain.port.TransactionPort;

public class SendChatMessageUseCase {
    private final MessageRepository messageRepository;
    private final NotificationPort notificationPort;
    private final TransactionPort transactionPort;
    private final LoggerPort logger;

    public SendChatMessageUseCase(MessageRepository messageRepository,
                                  NotificationPort notificationPort,
                                  TransactionPort transactionPort,
                                  LoggerPort logger) {
        this.messageRepository = messageRepository;
        this.notificationPort = notificationPort;
        this.transactionPort = transactionPort;
        this.logger = logger;
    }

    public void execute(SendChatMessageCommand command) {
        logger.info("Sending message to room {} from user {}", command.roomId(), command.userId());
        transactionPort.executeInTransaction(() -> {
            String userName = command.userName() != null ? command.userName() : "Anonymous";
            Message message = Message.createNew(
                command.roomId(),
                command.userId(),
                userName,
                command.content()
            );
            Message saved = messageRepository.save(message);
            // Отправить сообщение всем в комнате через WebSocket
            notificationPort.sendToRoom(command.roomId(), saved);
            return saved;
        });
    }
}