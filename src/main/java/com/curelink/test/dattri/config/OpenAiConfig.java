package com.curelink.test.dattri.config;

import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.curelink.test.dattri.llm.OpenAiProperties;

/**
 * Configures the {@link RestClient} used by {@link com.curelink.test.dattri.llm.OpenAiClientImpl}.
 *
 * Uses {@link JdkClientHttpRequestFactory} backed by {@link java.net.http.HttpClient}.
 * When {@code openai.skip-ssl-validation=true} (local dev only), a trust-all SSLContext
 * is used to bypass JVM cacerts issues. Never enable in production.
 */
@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    private static final Logger log = LoggerFactory.getLogger(OpenAiConfig.class);

    @Bean("openAiRestClient")
    public RestClient openAiRestClient(OpenAiProperties props)
            throws NoSuchAlgorithmException, KeyManagementException {

        Duration timeout = Duration.ofSeconds(props.getTimeoutSeconds());
        HttpClient httpClient = buildHttpClient(props.isSkipSslValidation(), timeout);

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(timeout);

        return RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + props.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(factory)
                .build();
    }

    private HttpClient buildHttpClient(boolean skipSslValidation, Duration timeout)
            throws NoSuchAlgorithmException, KeyManagementException {

        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(timeout);

        if (skipSslValidation) {
            log.warn("OpenAI client: SSL validation DISABLED (openai.skip-ssl-validation=true). " +
                    "Do NOT use this in production.");
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts(), new SecureRandom());
            builder.sslContext(sslContext);
        }

        return builder.build();
    }

    private static TrustManager[] trustAllCerts() {
        return new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };
    }
}
