package com.curelink.test.dattri.llm.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for POST /v1/chat/completions.
 */
public record ChatCompletionRequest(
    @JsonProperty("model")      String model,
    @JsonProperty("messages")   List<LlmMessage> messages,
    @JsonProperty("max_tokens") int maxTokens,
    @JsonProperty("temperature") double temperature
) {}
