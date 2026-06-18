package com.fatoldfool.chatbot.infrastructure.logging;

import com.fatoldfool.chatbot.domain.port.LoggerPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Slf4jLoggerAdapter implements LoggerPort {
    private final Logger logger;

    public Slf4jLoggerAdapter() {
        // Получаем имя класса-вызывающего, но для простоты используем корневой логгер.
        // В реальности можно передавать имя класса в конструктор.
        this.logger = LoggerFactory.getLogger("ChatBot");
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, Object... args) {
        if (args == null || args.length == 0) {
            logger.info(message);
        } else {
            logger.info(message, args);
        }
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }
}