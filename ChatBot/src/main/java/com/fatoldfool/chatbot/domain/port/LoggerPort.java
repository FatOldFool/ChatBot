package com.fatoldfool.chatbot.domain.port;

public interface LoggerPort {
    void info(String message);
    void info(String message, Object... args);
    void error(String message);
    void error(String message, Throwable throwable);
    void warn(String message);
    void debug(String message);
}