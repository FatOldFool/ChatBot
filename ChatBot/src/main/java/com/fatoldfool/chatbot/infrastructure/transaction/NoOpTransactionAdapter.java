package com.fatoldfool.chatbot.infrastructure.transaction;

import com.fatoldfool.chatbot.domain.port.TransactionPort;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class NoOpTransactionAdapter implements TransactionPort {
    @Override
    public <T> T executeInTransaction(Supplier<T> operation) {
        return operation.get();
    }

    @Override
    public void executeInTransaction(Runnable operation) {
        operation.run();
    }
}