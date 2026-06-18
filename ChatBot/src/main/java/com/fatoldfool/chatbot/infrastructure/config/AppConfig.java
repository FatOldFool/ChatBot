package com.fatoldfool.chatbot.infrastructure.config;

import com.fatoldfool.chatbot.application.usecase.CreateRoomUseCase;
import com.fatoldfool.chatbot.application.usecase.GetRoomMessagesUseCase;
import com.fatoldfool.chatbot.application.usecase.SendChatMessageUseCase;
import com.fatoldfool.chatbot.domain.port.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // Все зависимости (ChatRoomRepository, MessageRepository, NotificationPort,
    // TransactionPort, LoggerPort) уже имеют аннотации @Component/@Repository,
    // поэтому Spring автоматически внедрит их в конструкторы use cases.
    // Явные бины ниже не обязательны, но оставлены для ясности.

    @Bean
    public CreateRoomUseCase createRoomUseCase(ChatRoomRepository chatRoomRepository,
                                               LoggerPort loggerPort) {
        return new CreateRoomUseCase(chatRoomRepository, loggerPort);
    }

    @Bean
    public SendChatMessageUseCase sendChatMessageUseCase(MessageRepository messageRepository,
                                                         NotificationPort notificationPort,
                                                         TransactionPort transactionPort,
                                                         LoggerPort loggerPort) {
        return new SendChatMessageUseCase(messageRepository, notificationPort, transactionPort, loggerPort);
    }

    @Bean
    public GetRoomMessagesUseCase getRoomMessagesUseCase(MessageRepository messageRepository,
                                                         LoggerPort loggerPort) {
        return new GetRoomMessagesUseCase(messageRepository, loggerPort);
    }
}