package com.curelink.test.dattri.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.curelink.test.dattri.dao.UserMemoryDao;
import com.curelink.test.dattri.entity.ChatMessage;
import com.curelink.test.dattri.entity.ChatSession;
import com.curelink.test.dattri.entity.UserMemory;
import com.curelink.test.dattri.llm.OpenAiClient;
import com.curelink.test.dattri.llm.dto.LlmMessage;

/**
 * Uses a secondary LLM call to extract persistent user facts from recent conversation
 * and stores them as {@link UserMemory} rows.
 *
 * Trigger strategy:
 *   - Messages 1-5  → always extract (onboarding phase, user reveals name/age/goals)
 *   - Message N>5   → extract every 10 messages (ongoing fact gathering)
 */
@Service
public class MemoryExtractorServiceImpl implements MemoryExtractorService {

    private static final Logger log = LoggerFactory.getLogger(MemoryExtractorServiceImpl.class);

    private static final int  ONBOARDING_THRESHOLD = 5;
    private static final int  EXTRACTION_INTERVAL  = 10;
    private static final int  MAX_EXTRACTION_MESSAGES = 10;

    private static final String NONE_MARKER = "NONE";

    private final OpenAiClient   openAiClient;
    private final UserMemoryDao  userMemoryDao;

    public MemoryExtractorServiceImpl(OpenAiClient openAiClient, UserMemoryDao userMemoryDao) {
        this.openAiClient  = openAiClient;
        this.userMemoryDao = userMemoryDao;
    }

    @Override
    public void extractAndSave(ChatSession session, List<ChatMessage> recentHistory, long messageCount) {
        if (!shouldExtract(messageCount)) {
            return;
        }
        if (recentHistory == null || recentHistory.isEmpty()) {
            return;
        }

        try {
            List<UserMemory> existing = userMemoryDao.findBySessionId(session.getId());
            Set<String> knownFacts = existing.stream()
                    .map(m -> m.getContent().toLowerCase().trim())
                    .collect(Collectors.toSet());

            List<LlmMessage> prompt = buildExtractionPrompt(recentHistory, existing);
            String raw = openAiClient.complete(prompt);

            List<String> newFacts = parseExtractedFacts(raw, knownFacts);
            if (newFacts.isEmpty()) {
                log.debug("Memory extraction: no new facts for session={}", session.getId());
                return;
            }

            for (String fact : newFacts) {
                userMemoryDao.save(new UserMemory(
                        UUID.randomUUID().toString(),
                        session,
                        fact,
                        Instant.now()
                ));
            }
            log.info("Memory extraction: saved {} new facts for session={}", newFacts.size(), session.getId());

        } catch (Exception e) {
            log.warn("Memory extraction failed for session={}: {}", session.getId(), e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    private boolean shouldExtract(long messageCount) {
        if (messageCount <= ONBOARDING_THRESHOLD) return true;
        return messageCount % EXTRACTION_INTERVAL == 0;
    }

    private List<LlmMessage> buildExtractionPrompt(
            List<ChatMessage> recentHistory,
            List<UserMemory> existing
    ) {
        StringBuilder systemPrompt = new StringBuilder("""
                You are a memory extraction assistant. \
                From the conversation below, extract any factual, persistent information \
                the user has revealed about themselves — such as their name, age, health conditions, \
                goals, preferences, medications, allergies, or lifestyle habits. \
                Return each fact as a single short sentence on its own line. \
                If there is nothing new to extract, return only the word NONE. \
                Do not include greetings, temporary states, or opinions. \
                Do not repeat facts already listed under "Already known".\
                """);

        if (!existing.isEmpty()) {
            systemPrompt.append("\n\nAlready known:\n");
            for (UserMemory m : existing) {
                systemPrompt.append("- ").append(m.getContent()).append("\n");
            }
        }

        List<LlmMessage> messages = new ArrayList<>();
        messages.add(LlmMessage.system(systemPrompt.toString()));

        // Include only the last N messages to keep the extraction prompt small
        List<ChatMessage> window = recentHistory.size() > MAX_EXTRACTION_MESSAGES
                ? recentHistory.subList(recentHistory.size() - MAX_EXTRACTION_MESSAGES, recentHistory.size())
                : recentHistory;

        StringBuilder conversation = new StringBuilder("Conversation:\n");
        for (ChatMessage msg : window) {
            String role = msg.getRole().name().equals("USER") ? "User" : "Coach";
            conversation.append(role).append(": ").append(msg.getContent()).append("\n");
        }
        messages.add(LlmMessage.user(conversation.toString()));

        return messages;
    }

    private List<String> parseExtractedFacts(String raw, Set<String> knownFacts) {
        List<String> results = new ArrayList<>();
        if (raw == null || raw.isBlank() || raw.trim().equalsIgnoreCase(NONE_MARKER)) {
            return results;
        }
        for (String line : raw.split("\\n")) {
            String fact = line.replaceAll("^[-•*\\d.]+\\s*", "").trim();
            if (fact.isEmpty() || fact.equalsIgnoreCase(NONE_MARKER)) continue;
            if (fact.length() > 500) fact = fact.substring(0, 500);
            if (!knownFacts.contains(fact.toLowerCase())) {
                results.add(fact);
                knownFacts.add(fact.toLowerCase()); // avoid duplicates within the same extraction batch
            }
        }
        return results;
    }
}
