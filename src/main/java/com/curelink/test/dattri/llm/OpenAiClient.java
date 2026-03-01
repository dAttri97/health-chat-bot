package com.curelink.test.dattri.llm;

import java.util.List;

import com.curelink.test.dattri.llm.dto.LlmMessage;

/**
 * Abstraction over the OpenAI Chat Completions API.
 * Keeps the rest of the code independent of the provider.
 */
public interface OpenAiClient {

    /**
     * Send a list of messages (system + history + user) and return the assistant reply text.
     *
     * @param messages ordered list of messages (system prompt first, then conversation)
     * @return assistant reply content
     * @throws OpenAiException on any API or network error
     */
    String complete(List<LlmMessage> messages);
}
