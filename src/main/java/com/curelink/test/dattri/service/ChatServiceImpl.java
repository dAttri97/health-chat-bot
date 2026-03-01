package com.curelink.test.dattri.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.curelink.test.dattri.controller.dto.MessageResponse;
import com.curelink.test.dattri.controller.dto.PaginatedMessagesResponse;
import com.curelink.test.dattri.dao.ChatMessageDao;
import com.curelink.test.dattri.dao.ChatSessionDao;
import com.curelink.test.dattri.dao.ProtocolDao;
import com.curelink.test.dattri.dao.UserMemoryDao;
import com.curelink.test.dattri.entity.ChatMessage;
import com.curelink.test.dattri.entity.ChatMessage.MessageRole;
import com.curelink.test.dattri.entity.ChatSession;
import com.curelink.test.dattri.entity.Protocol;
import com.curelink.test.dattri.entity.UserMemory;
import com.curelink.test.dattri.llm.OpenAiClient;
import com.curelink.test.dattri.llm.dto.LlmMessage;
import com.curelink.test.dattri.redis.IRedisService;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    /**
     * Number of most-recent messages sent to the LLM as conversation context.
     * Capped to avoid hitting token limits.
     */
    private static final int MAX_CONTEXT_MESSAGES = 20;

    /** Redis key prefix for the typing indicator flag. */
    private static final String TYPING_KEY_PREFIX = "typing:";

    /** TTL for the typing flag — auto-clears if the app crashes mid-generation. */
    private static final long TYPING_TTL_SECONDS = 60;

    private final ChatSessionDao chatSessionDao;
    private final ChatMessageDao chatMessageDao;
    private final UserMemoryDao userMemoryDao;
    private final ProtocolDao protocolDao;
    private final OpenAiClient openAiClient;
    private final IRedisService redisService;
    private final PromptBuilder promptBuilder;
    private final MemoryExtractorService memoryExtractorService;

    public ChatServiceImpl(
            ChatSessionDao chatSessionDao,
            ChatMessageDao chatMessageDao,
            UserMemoryDao userMemoryDao,
            ProtocolDao protocolDao,
            OpenAiClient openAiClient,
            IRedisService redisService,
            PromptBuilder promptBuilder,
            MemoryExtractorService memoryExtractorService
    ) {
        this.chatSessionDao         = chatSessionDao;
        this.chatMessageDao         = chatMessageDao;
        this.userMemoryDao          = userMemoryDao;
        this.protocolDao            = protocolDao;
        this.openAiClient           = openAiClient;
        this.redisService           = redisService;
        this.promptBuilder          = promptBuilder;
        this.memoryExtractorService = memoryExtractorService;
    }

    // -------------------------------------------------------------------------
    // sendMessage
    // -------------------------------------------------------------------------

    @Override
    public MessageResponse sendMessage(String sessionId, String content) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("sessionId must not be blank");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("message content must not be blank");
        }

        // 1. Resolve (or create) the session
        ChatSession session = chatSessionDao.findOrCreateBySessionId(sessionId);

        // 2. Persist the user's message
        ChatMessage userMessage = chatMessageDao.save(new ChatMessage(
                UUID.randomUUID().toString(),
                session,
                MessageRole.USER,
                content.trim(),
                Instant.now()
        ));
        log.debug("Persisted user message id={} session={}", userMessage.getId(), session.getId());

        // 3. Load context
        List<ChatMessage> recentHistory  = loadHistory(session.getId(), userMessage.getId());
        List<UserMemory>  memories       = userMemoryDao.findBySessionId(session.getId());
        List<Protocol>    protocols      = protocolDao.findAll();

        // 4. Build LLM messages
        List<LlmMessage> llmMessages = promptBuilder.build(content, recentHistory, memories, protocols);

        // 5. Set typing indicator
        setTyping(session.getId(), true);

        // 6. Call LLM
        String replyContent;
        try {
            replyContent = openAiClient.complete(llmMessages);
        } finally {
            // Always clear typing flag — even on LLM error
            setTyping(session.getId(), false);
        }

        // 7. Persist the assistant reply
        ChatMessage assistantMessage = chatMessageDao.save(new ChatMessage(
                UUID.randomUUID().toString(),
                session,
                MessageRole.ASSISTANT,
                replyContent,
                Instant.now()
        ));
        log.debug("Persisted assistant message id={} session={}", assistantMessage.getId(), session.getId());

        // 8. Extract and persist long-term memories (best-effort, never fails the request)
        long messageCount = chatMessageDao.countBySessionId(session.getId());
        memoryExtractorService.extractAndSave(session, recentHistory, messageCount);

        return toDto(assistantMessage);
    }

    // -------------------------------------------------------------------------
    // getMessages
    // -------------------------------------------------------------------------

    @Override
    public PaginatedMessagesResponse getMessages(String sessionId, String beforeMessageId, int limit) {
        ChatSession session = chatSessionDao.findOrCreateBySessionId(sessionId);

        // Fetch one extra to determine whether there are more pages
        int fetchSize = limit + 1;

        List<ChatMessage> raw;
        if (beforeMessageId == null || beforeMessageId.isBlank()) {
            raw = chatMessageDao.findLatestBySessionId(session.getId(), fetchSize);
        } else {
            raw = chatMessageDao.findBeforeBySessionId(session.getId(), beforeMessageId, fetchSize);
        }

        boolean hasMore = raw.size() > limit;
        List<ChatMessage> page = hasMore ? raw.subList(0, limit) : raw;

        List<MessageResponse> dtos = page.stream().map(this::toDto).toList();

        // nextBefore is the id of the oldest message in the current page —
        // the frontend passes it as the cursor for the next "load more" call.
        String nextBefore = (hasMore && !page.isEmpty()) ? page.get(page.size() - 1).getId() : null;

        return new PaginatedMessagesResponse(dtos, hasMore, nextBefore);
    }

    // -------------------------------------------------------------------------
    // isTyping
    // -------------------------------------------------------------------------

    @Override
    public boolean isTyping(String sessionId) {
        ChatSession session = chatSessionDao.findOrCreateBySessionId(sessionId);
        String value = redisService.getValue(typingKey(session.getId()));
        return "true".equals(value);
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    /**
     * Load the most recent {@value MAX_CONTEXT_MESSAGES} messages for the session,
     * excluding the message just persisted (which will be the last entry in the list anyway),
     * then reverse from newest-first to chronological order for the LLM.
     */
    private List<ChatMessage> loadHistory(String entitySessionId, String excludeMessageId) {
        List<ChatMessage> newest = chatMessageDao.findLatestBySessionId(
                entitySessionId, MAX_CONTEXT_MESSAGES + 1);

        List<ChatMessage> filtered = new ArrayList<>();
        for (ChatMessage m : newest) {
            if (!m.getId().equals(excludeMessageId)) {
                filtered.add(m);
                if (filtered.size() == MAX_CONTEXT_MESSAGES) break;
            }
        }
        // Repository returns newest-first; LLM expects chronological (oldest first)
        Collections.reverse(filtered);
        return filtered;
    }

    private void setTyping(String entitySessionId, boolean typing) {
        try {
            String key = typingKey(entitySessionId);
            if (typing) {
                redisService.setValue(key, "true", TYPING_TTL_SECONDS, TimeUnit.SECONDS);
            } else {
                redisService.delete(key);
            }
        } catch (Exception e) {
            // Typing indicator is best-effort; never fail a message send because of Redis
            log.warn("Failed to update typing flag for session {}: {}", entitySessionId, e.getMessage());
        }
    }

    private static String typingKey(String entitySessionId) {
        return TYPING_KEY_PREFIX + entitySessionId;
    }

    private MessageResponse toDto(ChatMessage msg) {
        return new MessageResponse(
                msg.getId(),
                msg.getRole().name().toLowerCase(),
                msg.getContent(),
                msg.getCreatedAt()
        );
    }
}
