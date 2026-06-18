package com.fatoldfool.chatbot.infrastructure.websocket;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionRegistry {
    private final ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Set<String>> roomUsers = new ConcurrentHashMap<>();

    public void joinRoom(String sessionId, Long roomId, String userName) {
        sessions.put(sessionId, new UserSession(roomId, userName));
        roomUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    public UserSession leaveRoom(String sessionId) {
        UserSession session = sessions.remove(sessionId);
        if (session != null) {
            Set<String> users = roomUsers.get(session.roomId());
            if (users != null) {
                users.remove(sessionId);
                if (users.isEmpty()) roomUsers.remove(session.roomId());
            }
        }
        return session;
    }

    public String getUserName(String sessionId) {
        UserSession session = sessions.get(sessionId);
        return session != null ? session.userName() : null;
    }

    public List<UserSession> getUsersInRoom(Long roomId) {
        Set<String> userIds = roomUsers.getOrDefault(roomId, Collections.emptySet());
        List<UserSession> result = new ArrayList<>();
        for (String sid : userIds) {
            UserSession us = sessions.get(sid);
            if (us != null) result.add(us);
        }
        return result;
    }

    public record UserSession(Long roomId, String userName) {}
}