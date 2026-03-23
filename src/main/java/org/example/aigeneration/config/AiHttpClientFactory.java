package org.example.aigeneration.config;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

@Slf4j
public final class AiHttpClientFactory {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(15);

    private AiHttpClientFactory() {
    }

    public static HttpClientBuilder create(Duration readTimeout) {
        Duration effectiveReadTimeout = readTimeout != null ? readTimeout : Duration.ofSeconds(60);
        JdkHttpClientBuilder builder = new JdkHttpClientBuilder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .readTimeout(effectiveReadTimeout);
        HttpClient.Builder httpClientBuilder = createHttpClientBuilder();
        if (httpClientBuilder != null) {
            builder.httpClientBuilder(httpClientBuilder);
        }
        return builder;
    }

    private static HttpClient.Builder createHttpClientBuilder() {
        String proxyUrl = firstNonBlank(
                System.getenv("HTTPS_PROXY"),
                System.getenv("https_proxy"),
                System.getenv("HTTP_PROXY"),
                System.getenv("http_proxy")
        );
        if (proxyUrl == null) {
            return null;
        }
        try {
            URI proxyUri = URI.create(proxyUrl);
            String host = proxyUri.getHost();
            int port = proxyUri.getPort();
            if (host == null || port < 0) {
                log.warn("Ignore invalid proxy url: {}", proxyUrl);
                return null;
            }
            String scheme = proxyUri.getScheme() != null ? proxyUri.getScheme() : "http";
            HttpClient.Builder httpClientBuilder = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(new InetSocketAddress(host, port)))
                    .connectTimeout(DEFAULT_CONNECT_TIMEOUT);
            log.info("AI HTTP client configured with proxy {}://{}:{}", scheme, host, port);
            return httpClientBuilder;
        } catch (Exception e) {
            log.warn("Failed to configure AI HTTP proxy from {}", proxyUrl, e);
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
