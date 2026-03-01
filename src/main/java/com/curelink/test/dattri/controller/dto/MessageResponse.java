package com.curelink.test.dattri.controller.dto;

import java.time.Instant;

/**
 * Single chat message in API responses.
 */
public record MessageResponse(
    String id,
    String role,
    String content,
    Instant createdAt
) {}
