package com.curelink.test.dattri.dao;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.curelink.test.dattri.entity.ChatSession;
import com.curelink.test.dattri.repository.ChatSessionRepository;

@Component
public class ChatSessionDaoImpl implements ChatSessionDao {

    private final ChatSessionRepository chatSessionRepository;

    public ChatSessionDaoImpl(ChatSessionRepository chatSessionRepository) {
        this.chatSessionRepository = chatSessionRepository;
    }

    @Override
    @Transactional
    public ChatSession findOrCreateBySessionId(String sessionId) {
        return chatSessionRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatSession session = new ChatSession(
                            UUID.randomUUID().toString(),
                            sessionId,
                            Instant.now()
                    );
                    return chatSessionRepository.save(session);
                });
    }
}
