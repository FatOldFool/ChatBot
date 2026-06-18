package com.fatoldfool.chatbot.domain.port;

import java.util.function.Supplier;

public interface TransactionPort {
    <T> T executeInTransaction(Supplier<T> operation);
    
    void executeInTransaction(Runnable operation);
}