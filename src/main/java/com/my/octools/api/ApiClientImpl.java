package com.my.octools.api;

import com.my.octools.api.entities.AppliancePage;
import com.my.octools.api.entities.DrainResult;
import com.my.octools.api.entities.RemediationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation to communicate with remote appliance APIs asynchronously.
 * <p>
 * Provides methods to fetch appliance data pages, drain appliances, remediate appliances,
 * with retry and error handling logic.
 */
@Component
public class ApiClientImpl implements ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private final WebClient webClient;

    /**
     * Constructs an ApiClientImpl with the given {@link WebClient}.
     *
     * @param webClient the WebClient used for HTTP requests
     */
    public ApiClientImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public CompletableFuture<AppliancePage> fetchAppliances(String after) {
        String uri = "/api/1.0/appliances?first=100" + (after != null ? "&after=" + after : "");
        return webClient.get().uri(uri)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            logger.error("Error during fetch for cursor " + after +
                                    ": HTTP " + response.statusCode() + " - Body: " + body);
                            return Mono.error(new RuntimeException("Fetch API error: " + body));
                        })
                )
                .bodyToMono(AppliancePage.class)
                .retryWhen(getRetrySpec())
                .toFuture();
    }

    @Override
    public CompletableFuture<DrainResult> drain(String id) {
        return webClient.post().uri("/api/1.0/appliances/" + id + "/drain")
                .bodyValue(Map.of("reason", "inactivity", "actor", "oct-app"))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            logger.error("Error during DRAIN for appliance " + id +
                                    ": HTTP " + response.statusCode() + " - Body: " + body);
                            return Mono.error(new RuntimeException("Drain API error: " + body));
                        })
                )
                .bodyToMono(DrainResult.class)
                .retryWhen(getRetrySpec())
                .toFuture();
    }

    @Override
    public CompletableFuture<RemediationResult> remediate(String id) {
        return webClient.post().uri("/api/1.0/appliances/" + id + "/remediate")
                .bodyValue(Map.of("reason", "remediated after drain", "actor", "oct-app"))
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class).flatMap(body -> {
                            logger.error("Error during REMEDIATE for appliance " + id +
                                    ": HTTP " + response.statusCode() + " - Body: " + body);
                            return Mono.error(new RuntimeException("Remediate API error: " + body));
                        })
                )
                .bodyToMono(RemediationResult.class)
                .retryWhen(getRetrySpec())
                .toFuture();
    }

    /**
     * Creates a {@link Retry} specification with exponential backoff and jitter.
     * Retries up to 3 times, starting with a delay of 3 seconds,
     * maximum backoff 10 seconds, and jitter of 0.5.
     * Logs warnings before retry and increments retry exhaustion count if retries are exhausted.
     *
     * @return configured Retry specification
     */
    private Retry getRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(3))
                .maxBackoff(Duration.ofSeconds(10))
                .jitter(0.5)
                .doBeforeRetry(retrySignal ->
                        logger.warn("Retrying due to: " + retrySignal.failure().getMessage())
                );
    }
}
