package com.curelink.test.dattri.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed config properties for OpenAI, bound from {@code openai.*} in application.properties.
 * Registered via {@code @EnableConfigurationProperties} in {@link com.curelink.test.dattri.config.OpenAiConfig}.
 */
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    private String apikey = "";
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4o";
    private int maxTokens = 1000;
    private double temperature = 0.7;
    private int timeoutSeconds = 30;
    private boolean skipSslValidation = false;

    public String getApiKey() { return apikey; }
    public void setApiKey(String apiKey) { this.apikey = apiKey != null ? apiKey : ""; }

    public boolean isSkipSslValidation() { return skipSslValidation; }
    public void setSkipSslValidation(boolean skipSslValidation) { this.skipSslValidation = skipSslValidation; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
