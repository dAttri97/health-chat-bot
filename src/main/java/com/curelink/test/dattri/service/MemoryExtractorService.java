package com.curelink.test.dattri.service;

import java.util.List;

import com.curelink.test.dattri.entity.ChatMessage;
import com.curelink.test.dattri.entity.ChatSession;
import com.curelink.test.dattri.entity.UserMemory;

/**
 * Extracts persistent user facts from recent conversation turns and saves them
 * as {@link UserMemory} rows using a secondary LLM call.
 *
 * Triggered after onboarding turns (first 5 messages) and every 10 messages.
 */
public interface MemoryExtractorService {

    /**
     * Inspect the conversation and persist any new facts about the user.
     * Never throws — failures are logged and swallowed so the main reply is unaffected.
     *
     * @param session        the current chat session
     * @param recentHistory  last N messages in chronological order
     * @param messageCount   total message count for the session (used to decide trigger frequency)
     */
    void extractAndSave(ChatSession session, List<ChatMessage> recentHistory, long messageCount);
}
