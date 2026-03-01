package com.curelink.test.dattri.service;

import com.curelink.test.dattri.controller.dto.MessageResponse;
import com.curelink.test.dattri.controller.dto.PaginatedMessagesResponse;

public interface ChatService {

    /**
     * Persist the user's message, call the LLM, persist the reply, and return the assistant message.
     * Sets a Redis typing flag while the LLM is generating.
     *
     * @param sessionId external session id (from API key)
     * @param content   user message text
     */
    MessageResponse sendMessage(String sessionId, String content);

    /**
     * Return paginated message history for the session, newest first.
     * Pass {@code beforeMessageId=null} for the latest page; pass the id of the
     * oldest already-held message to load older history (scroll-up).
     *
     * @param sessionId       external session id
     * @param beforeMessageId cursor; null = latest page
     * @param limit           max messages to return (capped by controller)
     */
    PaginatedMessagesResponse getMessages(String sessionId, String beforeMessageId, int limit);

    /**
     * Whether the coach is currently generating a reply for this session.
     * Backed by a Redis key with a short TTL so the flag self-clears on crash.
     */
    boolean isTyping(String sessionId);
}
