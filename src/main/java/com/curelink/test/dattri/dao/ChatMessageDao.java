package com.curelink.test.dattri.dao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.curelink.test.dattri.entity.ChatMessage;
import com.curelink.test.dattri.entity.ChatSession;

/**
 * Data access for chat messages (persist and paginated read).
 */
public interface ChatMessageDao {

    ChatMessage save(ChatMessage message);

    Optional<ChatMessage> findById(String id);

    /**
     * Latest messages for a session, newest first. For initial load or "scroll to bottom".
     *
     * @param sessionId session entity id (ChatSession.getId()), not the external session id
     * @param limit     max number of messages
     */
    List<ChatMessage> findLatestBySessionId(String sessionId, int limit);

    /**
     * Older messages before a given message (for "load more" on scroll up).
     *
     * @param sessionId     session entity id
     * @param beforeMessageId id of the oldest message we already have
     * @param limit         max number of messages
     */
    List<ChatMessage> findBeforeBySessionId(String sessionId, String beforeMessageId, int limit);

    long countBySessionId(String sessionId);
}
