package com.curelink.test.dattri.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.curelink.test.dattri.auth.ApiKeyAuthenticationToken;
import com.curelink.test.dattri.controller.dto.MessageResponse;
import com.curelink.test.dattri.controller.dto.PaginatedMessagesResponse;
import com.curelink.test.dattri.controller.dto.SendMessageRequest;
import com.curelink.test.dattri.controller.dto.TypingStatusResponse;
import com.curelink.test.dattri.service.ChatService;

/**
 * REST API for the mini AI health coach chat.
 * Delegates all business logic to {@link ChatService}.
 * All endpoints require API key authentication (X-API-Key or Authorization header).
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final int MAX_LIMIT = 50;

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Send a message and get the coach reply.
     * Blocks until the LLM generates a response (synchronous).
     * Frontend should optimistically render the user message immediately,
     * then append the returned assistant message on response.
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            Authentication auth,
            @Valid @RequestBody SendMessageRequest request
    ) {
        String sessionId = sessionId(auth);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        MessageResponse reply = chatService.sendMessage(sessionId, request.content());
        return ResponseEntity.ok(reply);
    }

    /**
     * Return messages newest-first.
     * No {@code before} param → latest page (for initial load / autoscroll to bottom).
     * {@code before=<messageId>} → older messages before that id (for scroll-up "load more").
     */
    @GetMapping("/messages")
    public ResponseEntity<PaginatedMessagesResponse> getMessages(
            Authentication auth,
            @RequestParam(required = false) String before,
            @RequestParam(defaultValue = "20") int limit
    ) {
        String sessionId = sessionId(auth);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        limit = Math.min(Math.max(1, limit), MAX_LIMIT);
        PaginatedMessagesResponse response = chatService.getMessages(sessionId, before, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Typing indicator: true while the coach is generating a reply.
     * Frontend polls this at ~1 s intervals after sending a message.
     */
    @GetMapping("/typing")
    public ResponseEntity<TypingStatusResponse> getTypingStatus(Authentication auth) {
        String sessionId = sessionId(auth);
        if (sessionId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new TypingStatusResponse(chatService.isTyping(sessionId)));
    }

    // -------------------------------------------------------------------------

    private static String sessionId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || !(auth instanceof ApiKeyAuthenticationToken token)) {
            return null;
        }
        return token.getSessionId();
    }
}
