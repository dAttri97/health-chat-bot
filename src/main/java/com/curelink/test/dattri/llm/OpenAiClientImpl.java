package com.curelink.test.dattri.llm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.curelink.test.dattri.llm.dto.ChatCompletionRequest;
import com.curelink.test.dattri.llm.dto.ChatCompletionResponse;
import com.curelink.test.dattri.llm.dto.LlmMessage;

/**
 * OpenAI Chat Completions client using Spring's {@link RestClient}.
 * Calls POST /v1/chat/completions with the configured model and parameters.
 */
@Component
public class OpenAiClientImpl implements OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClientImpl.class);
    private static final String COMPLETIONS_PATH = "/chat/completions";
    private static final String DEFAULT_FALLBACK_RESPONSE =
            "I'm having a little trouble right now. Please try again in a moment.";

    private final RestClient restClient;
    private final OpenAiProperties props;

    public OpenAiClientImpl(
            @Qualifier("openAiRestClient") RestClient restClient,
            OpenAiProperties props
    ) {
        this.restClient = restClient;
        this.props = props;
    }

    @Override
    public String complete(List<LlmMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new OpenAiException("Message list must not be empty", HttpStatus.BAD_REQUEST.value());
        }

        ChatCompletionRequest request = new ChatCompletionRequest(
                props.getModel(),
                messages,
                props.getMaxTokens(),
                props.getTemperature()
        );

        log.debug("Calling OpenAI model={} messages={}", props.getModel(), messages.size());

        try {
            ChatCompletionResponse response = restClient.post()
                    .uri(COMPLETIONS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response == null) {
                log.error("Empty response from OpenAI");
                return DEFAULT_FALLBACK_RESPONSE;
            }

            String content = response.firstContent();
            if (content.isBlank()) {
                log.error("OpenAI returned empty content, finish_reason={}",
                        response.choices().isEmpty() ? "none" : response.choices().get(0).finishReason());
                return DEFAULT_FALLBACK_RESPONSE;
            }

            log.debug("OpenAI usage: {}", response.usage());
            return content;

        } catch (HttpClientErrorException e) {
            log.error("OpenAI client error status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return DEFAULT_FALLBACK_RESPONSE;
        } catch (HttpServerErrorException e) {
            log.error("OpenAI server error status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return DEFAULT_FALLBACK_RESPONSE;
        } catch (OpenAiException e) {
            log.error("OpenAI error: {}", e.getMessage());
            return DEFAULT_FALLBACK_RESPONSE;
        } catch (Exception e) {
            log.error("Unexpected error calling OpenAI", e);
            return DEFAULT_FALLBACK_RESPONSE;
        }
    }
}
