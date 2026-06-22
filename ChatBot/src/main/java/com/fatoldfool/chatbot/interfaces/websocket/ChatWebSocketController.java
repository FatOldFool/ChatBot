package com.fatoldfool.chatbot.interfaces.websocket;

import com.fatoldfool.chatbot.application.command.SendChatMessageCommand;
import com.fatoldfool.chatbot.application.usecase.SendChatMessageUseCase;
import com.fatoldfool.chatbot.domain.exception.InvalidMessageException;
import com.fatoldfool.chatbot.domain.exception.TooManyRequestsException;
import com.fatoldfool.chatbot.domain.port.NotificationPort;
import com.fatoldfool.chatbot.infrastructure.ratelimit.RateLimiter;
import com.fatoldfool.chatbot.infrastructure.websocket.UserSessionRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    private final SendChatMessageUseCase sendChatMessageUseCase;
    private final NotificationPort notificationPort;
    private final UserSessionRegistry sessionRegistry;
    private final RateLimiter rateLimiter;

    public ChatWebSocketController(SendChatMessageUseCase sendChatMessageUseCase,
                                   NotificationPort notificationPort,
                                   UserSessionRegistry sessionRegistry,
                                   RateLimiter rateLimiter) {
        this.sendChatMessageUseCase = sendChatMessageUseCase;
        this.notificationPort = notificationPort;
        this.sessionRegistry = sessionRegistry;
        this.rateLimiter = rateLimiter;
    }

    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, Map<String, String> payload,
                            Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String username = principal.getName(); // из токена
        if (!rateLimiter.tryAcquire("sendMessage:" + username, 10, 10)) {
            throw new TooManyRequestsException("Слишком много сообщений. Подождите немного.");
        }

        String content = payload.get("content");
        SendChatMessageCommand command = new SendChatMessageCommand(roomId, username, content);
        sendChatMessageUseCase.execute(command);
    }

    @MessageMapping("/chat.join/{roomId}")
    public void joinRoom(@DestinationVariable Long roomId, Principal principal,
                         SimpMessageHeaderAccessor headerAccessor) {
        String username = principal.getName();
        String sessionId = headerAccessor.getSessionId();
        sessionRegistry.joinRoom(sessionId, roomId, username);
        notificationPort.sendToRoom(roomId, Map.of(
                "type", "system",
                "content", username + " присоединился к комнате",
                "createdAt", Instant.now().toString()
        ));
    }

    @MessageMapping("/chat.typing/{roomId}")
    public void typing(@DestinationVariable Long roomId, Principal principal,
                       SimpMessageHeaderAccessor headerAccessor) {
        String username = principal.getName();
        notificationPort.sendToRoom(roomId, Map.of(
                "type", "typing",
                "userName", username,
                "typing", true
        ));
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        UserSessionRegistry.UserSession session = sessionRegistry.leaveRoom(sessionId);
        if (session != null) {
            notificationPort.sendToRoom(session.roomId(), Map.of(
                    "type", "system",
                    "content", session.userName() + " покинул комнату",
                    "createdAt", Instant.now().toString()
            ));
        }
    }

    @MessageExceptionHandler(InvalidMessageException.class)
    public void handleInvalidMessageException(InvalidMessageException ex, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        if (sessionId != null) {
            notificationPort.sendToUser(sessionId, Map.of(
                    "type", "error",
                    "message", ex.getMessage()
            ));
        }
    }

    @MessageExceptionHandler(TooManyRequestsException.class)
    public void handleTooManyRequestsException(TooManyRequestsException ex, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        if (sessionId != null) {
            notificationPort.sendToUser(sessionId, Map.of(
                    "type", "error",
                    "message", ex.getMessage()
            ));
        }
    }
}