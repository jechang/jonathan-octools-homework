package com.my.octools.service;

import com.google.common.annotations.VisibleForTesting;
import com.my.octools.api.ApiClient;
import com.my.octools.api.entities.Appliance;
import com.my.octools.api.entities.DrainResult;
import com.my.octools.storage.ApplianceLog;
import com.my.octools.storage.ApplianceLogRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service to process appliances by fetching, filtering, draining, remediating, and logging results.
 */
@Service
public class ApplianceProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ApplianceProcessor.class);

    private static final int API_CALL_TIMEOUT_SECONDS = 20;
    private static final int STALE_MINUTES_THRESHOLD = 10;

    private final ApiClient client;
    private final ApplianceLogRepository logStore;
    private final ScheduledExecutorService executor;

    public ApplianceProcessor(ApiClient client, ApplianceLogRepository logStore, ScheduledExecutorService executor) {
        this.client = client;
        this.logStore = logStore;
        this.executor = executor;
    }

    /**
     * Initializes the processor by running the job once immediately after construction.
     */
    @PostConstruct
    public void init() {
        logger.info("Running job immediately at startup...");
        runJob();
    }

    /**
     * Scheduled job to fetch, process, and log appliance data every 5 minutes.
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void runJob() {
        logger.info("Starting scheduled job...");
        fetchAndProcessAllAppliances()
                .exceptionally(ex -> {
                    logger.error("Scheduled job failed: {}", ex.getMessage(), ex);
                    return null;
                });
    }

    @VisibleForTesting
    CompletableFuture<Void> fetchAndProcessAllAppliances() {
        return fetchAndProcessPage(null);
    }

    private CompletableFuture<Void> fetchAndProcessPage(String cursor) {
        return client.fetchAppliances(cursor)
                .orTimeout(API_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .thenCompose(page -> {
                    List<Appliance> pageData = page.getData();

                    return drainAndRemediateAppliances(pageData)
                            .thenCompose(v -> {
                                if (page.getPageInfo().isHasNextPage()) {
                                    return fetchAndProcessPage(page.getPageInfo().getEndCursor());
                                } else {
                                    return CompletableFuture.completedFuture(null);
                                }
                            });
                })
                .exceptionally(ex -> {
                    logger.error("Error fetching appliances after cursor {}: {}", cursor, ex.getMessage(), ex);
                    return null; // return a completed future with void
                });
    }

    /**
     * Filters appliances to drain/remediate and processes them asynchronously.
     *
     * @param appliances list of appliances to filter and process
     * @return CompletableFuture that completes when all processing is finished
     */
    private CompletableFuture<Void> drainAndRemediateAppliances(List<Appliance> appliances) {
        List<Appliance> filteredAppliancesToDrain = getFilteredAppliances(appliances);
        if (filteredAppliancesToDrain.isEmpty()) {
            logger.info("No appliances matched the filter criteria. Skipping processing.");
        }
        List<CompletableFuture<Void>> processedFilteredAppliances = filteredAppliancesToDrain.stream()
                .map(appliance -> drainAndRemediateSingleAppliance(appliance.getId()))
                .toList();

        return CompletableFuture.allOf(processedFilteredAppliances.toArray(new CompletableFuture[0]));
    }

    /**
     * Processes a single appliance by draining and remediating it asynchronously.
     *
     * @param applianceId appliance identifier
     * @return CompletableFuture that completes when processing finishes
     */
    @VisibleForTesting
    CompletableFuture<Void> drainAndRemediateSingleAppliance(String applianceId) {
        return client.drain(applianceId)
                .orTimeout(API_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .thenComposeAsync(drainResult -> handleDrainSuccess(applianceId, drainResult), executor)
                .exceptionally(ex -> {
                    logger.error("Error processing appliance {}: {}", applianceId, ex.getMessage(), ex);
                    logStore.save(new ApplianceLog(applianceId, "PROCESS", false, ex.getMessage(), Instant.now()));
                    return null;
                });
    }

    private CompletableFuture<Void> handleDrainSuccess(String applianceId, DrainResult drainResult) {
        logger.info("Appliance " + applianceId + " has been drained with result: " + drainResult);
        logStore.save(new ApplianceLog(applianceId, "DRAIN", true,
                drainResult.toString(), Instant.now()));
        return client.remediate(applianceId)
                .orTimeout(API_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .thenAcceptAsync(remediateResult -> {
                    logger.info("Appliance " + applianceId + " has been remediated with result: " + remediateResult);
                    logStore.save(new ApplianceLog(applianceId, "REMEDIATE", true,
                            remediateResult.toString(), Instant.now()));
                }, executor);
    }

    @VisibleForTesting
    List<Appliance> getFilteredAppliances(List<Appliance> appliances) {
        return appliances.stream()
                .filter(ap -> "LIVE".equals(ap.getOpStatus()) &&
                        (ap.getLastHeardFromOn() == null ||
                                Duration.between(ap.getLastHeardFromOn(), Instant.now()).toMinutes() > STALE_MINUTES_THRESHOLD))
                .collect(Collectors.toList());
    }
}