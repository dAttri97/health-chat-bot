package com.curelink.test.dattri.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.curelink.test.dattri.entity.ChatSession;

public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    Optional<ChatSession> findBySessionId(String sessionId);
}
