package com.curelink.test.dattri.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Protocol or guideline (e.g. fever, stomach ache, refund policy).
 * Matched with user queries and injected as context for the LLM.
 */
@Entity
@Table(name = "protocol")
public class Protocol {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "code", unique = true, nullable = false, length = 100)
    private String code;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Protocol() {}

    public Protocol(String id, String code, String title, String content, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.title = title != null ? title : "";
        this.content = content;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
