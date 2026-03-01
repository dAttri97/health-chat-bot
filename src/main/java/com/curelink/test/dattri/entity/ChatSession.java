package com.curelink.test.dattri.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * A single chat session (one per user / API key).
 * Holds the conversation and is used to scope messages and long-term memory.
 */
@Entity
@Table(name = "chat_session")
public class ChatSession {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "session_id", unique = true, nullable = false, length = 255)
    private String sessionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMemory> memories = new ArrayList<>();

    protected ChatSession() {}

    public ChatSession(String id, String sessionId, Instant createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public List<UserMemory> getMemories() {
        return memories;
    }
}
