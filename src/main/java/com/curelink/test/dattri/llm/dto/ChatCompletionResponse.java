package com.curelink.test.dattri.llm.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body from POST /v1/chat/completions.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatCompletionResponse(
    @JsonProperty("id")      String id,
    @JsonProperty("choices") List<Choice> choices,
    @JsonProperty("usage")   Usage usage
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(
        @JsonProperty("index")         int index,
        @JsonProperty("message")       LlmMessage message,
        @JsonProperty("finish_reason") String finishReason
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
        @JsonProperty("prompt_tokens")     int promptTokens,
        @JsonProperty("completion_tokens") int completionTokens,
        @JsonProperty("total_tokens")      int totalTokens
    ) {}

    public String firstContent() {
        if (choices == null || choices.isEmpty()) return "";
        LlmMessage msg = choices.get(0).message();
        return msg != null && msg.content() != null ? msg.content().trim() : "";
    }
}
