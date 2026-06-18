package com.fatoldfool.chatbot.interfaces.websocket;

import com.fatoldfool.chatbot.application.command.SendChatMessageCommand;
import com.fatoldfool.chatbot.application.usecase.SendChatMessageUseCase;
import com.fatoldfool.chatbot.infrastructure.websocket.UserSessionRegistry;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Controller
public class ChatWebSocketController {

    private final SendChatMessageUseCase sendChatMessageUseCase;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserSessionRegistry sessionRegistry;

    public ChatWebSocketController(SendChatMessageUseCase sendChatMessageUseCase,
                                   SimpMessagingTemplate messagingTemplate,
                                   UserSessionRegistry sessionRegistry) {
        this.sendChatMessageUseCase = sendChatMessageUseCase;
        this.messagingTemplate = messagingTemplate;
        this.sessionRegistry = sessionRegistry;
    }

    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, Map<String, String> payload) {
        String userId = payload.get("userId");
        String userName = payload.get("userName");
        String content = payload.get("content");
        SendChatMessageCommand command = new SendChatMessageCommand(roomId, userId, userName, content);
        sendChatMessageUseCase.execute(command);
    }

    @MessageMapping("/chat.join/{roomId}")
    public void joinRoom(@DestinationVariable Long roomId, Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String userName = payload.get("userName");
        if (userName == null || userName.isBlank()) {
            userName = "User_" + sessionId.substring(0, 4);
        }
        sessionRegistry.joinRoom(sessionId, roomId, userName);
        String systemMessage = userName + " присоединился к комнате";
        messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                "type", "system",
                "content", systemMessage,
                "createdAt", java.time.Instant.now().toString()
        ));
    }

    @MessageMapping("/chat.typing/{roomId}")
    public void typing(@DestinationVariable Long roomId, Map<String, String> payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String userName = sessionRegistry.getUserName(sessionId); // нужно добавить метод getUserName
        if (userName == null) return;
        // Отправляем всем в комнату, кроме отправителя
        messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                "type", "typing",
                "userName", userName,
                "typing", true
        ));
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        UserSessionRegistry.UserSession session = sessionRegistry.leaveRoom(sessionId);
        if (session != null) {
            String userName = session.userName();
            String systemMessage = userName + " покинул комнату";
            messagingTemplate.convertAndSend("/topic/room/" + session.roomId(), Map.of(
                    "type", "system",
                    "content", systemMessage,
                    "createdAt", java.time.Instant.now().toString()
            ));
        }
    }
}