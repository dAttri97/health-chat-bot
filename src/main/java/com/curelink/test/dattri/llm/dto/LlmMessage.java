package com.curelink.test.dattri.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single message in an OpenAI chat completion request or response.
 */
public record LlmMessage(
    @JsonProperty("role")    String role,
    @JsonProperty("content") String content
) {
    public static LlmMessage system(String content)    { return new LlmMessage("system",    content); }
    public static LlmMessage user(String content)      { return new LlmMessage("user",      content); }
    public static LlmMessage assistant(String content) { return new LlmMessage("assistant", content); }
}
