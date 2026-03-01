package com.curelink.test.dattri.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for sending a chat message.
 */
public record SendMessageRequest(

    @NotBlank(message = "Message content is required")
    @Size(max = 10_000)
    String content
) {}
