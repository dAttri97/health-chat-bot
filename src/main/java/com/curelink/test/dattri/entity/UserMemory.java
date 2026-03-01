package com.curelink.test.dattri.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Long-term memory for a user/session (e.g. extracted facts, preferences).
 * Used as context when generating assistant replies.
 */
@Entity
@Table(name = "user_memory")
public class UserMemory {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession chatSession;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserMemory() {}

    public UserMemory(String id, ChatSession chatSession, String content, Instant createdAt) {
        this.id = id;
        this.chatSession = chatSession;
        this.content = content;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getId() {
        return id;
    }

    public ChatSession getChatSession() {
        return chatSession;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
