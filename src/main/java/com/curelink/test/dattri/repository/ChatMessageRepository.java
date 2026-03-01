package com.curelink.test.dattri.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.curelink.test.dattri.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    /**
     * Messages for a session, newest first.
     * For "before" cursor pagination: pass the createdAt of the last message you have.
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.chatSession.id = :sessionId AND (:before is null OR m.createdAt < :before) ORDER BY m.createdAt DESC")
    List<ChatMessage> findBySessionIdOrderByCreatedAtDesc(
            @Param("sessionId") String sessionId,
            @Param("before") java.time.Instant before,
            Pageable pageable);

    default List<ChatMessage> findLatestBySessionId(String sessionId, int limit) {
        return findBySessionIdOrderByCreatedAtDesc(sessionId, null, Pageable.ofSize(limit));
    }

    default List<ChatMessage> findBeforeBySessionId(String sessionId, java.time.Instant before, int limit) {
        return findBySessionIdOrderByCreatedAtDesc(sessionId, before, Pageable.ofSize(limit));
    }

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chatSession.id = :sessionId")
    long countBySessionId(@Param("sessionId") String sessionId);
}
