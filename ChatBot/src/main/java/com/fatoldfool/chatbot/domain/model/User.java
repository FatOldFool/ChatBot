package com.fatoldfool.chatbot.domain.model;

import java.util.Objects;

public class User {
    private final String sessionId;   // уникальный идентификатор для каждого браузера/вкладки
    private String displayName;       // опционально, можно задать

    public User(String sessionId) {
        this.sessionId = sessionId;
        this.displayName = "User-" + sessionId.substring(0, 6);
    }

    public User(String sessionId, String displayName) {
        this.sessionId = sessionId;
        this.displayName = displayName;
    }

    public String getSessionId() { return sessionId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(sessionId, user.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }
}