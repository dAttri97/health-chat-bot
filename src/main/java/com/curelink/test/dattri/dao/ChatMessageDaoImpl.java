package com.curelink.test.dattri.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.curelink.test.dattri.entity.ChatMessage;
import com.curelink.test.dattri.repository.ChatMessageRepository;

@Component
public class ChatMessageDaoImpl implements ChatMessageDao {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageDaoImpl(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    @Transactional
    public ChatMessage save(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    @Override
    public Optional<ChatMessage> findById(String id) {
        return chatMessageRepository.findById(id);
    }

    @Override
    public List<ChatMessage> findLatestBySessionId(String sessionId, int limit) {
        return chatMessageRepository.findLatestBySessionId(sessionId, limit);
    }

    @Override
    public List<ChatMessage> findBeforeBySessionId(String sessionId, String beforeMessageId, int limit) {
        return chatMessageRepository.findById(beforeMessageId)
                .map(msg -> chatMessageRepository.findBeforeBySessionId(sessionId, msg.getCreatedAt(), limit))
                .orElse(List.of());
    }

    @Override
    public long countBySessionId(String sessionId) {
        return chatMessageRepository.countBySessionId(sessionId);
    }
}
