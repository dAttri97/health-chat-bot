package com.curelink.test.dattri.dao;

import com.curelink.test.dattri.entity.ChatSession;

/**
 * Data access for chat sessions. Session id is the external identifier (e.g. API key).
 */
public interface ChatSessionDao {

    /**
     * Find session by external session id (e.g. API key), or create one if missing.
     */
    ChatSession findOrCreateBySessionId(String sessionId);
}
