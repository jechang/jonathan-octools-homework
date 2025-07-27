package com.my.octools.service;

import com.my.octools.api.ApiClient;
import com.my.octools.api.entities.*;
import com.my.octools.storage.ApplianceLog;
import com.my.octools.storage.ApplianceLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplianceProcessorTest {
    ApiClient client;
    ApplianceLogRepository logRepo;
    ScheduledExecutorService executor;
    ApplianceProcessor processor;

    @BeforeEach
    void setup() {
        client = mock(ApiClient.class);
        logRepo = mock(ApplianceLogRepository.class);
        executor = Executors.newSingleThreadScheduledExecutor();
        processor = new ApplianceProcessor(client, logRepo, executor);
    }

    @Test
    void testFetchAndProcessAllAppliances() {
        // Setup
        Appliance appliance = new Appliance();
        appliance.setId("appliance1");
        appliance.setOpStatus("LIVE");
        appliance.setLastHeardFromOn(null);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setHasNextPage(false);
        AppliancePage page = new AppliancePage();
        page.setPageInfo(pageInfo);
        page.setData(Collections.singletonList(appliance));

        DrainResult drainResult = new DrainResult();
        drainResult.setDrainId("drain1");
        drainResult.setEstimatedTimeToDrain("5m");

        RemediationResult remediationResult = new RemediationResult();
        remediationResult.setRemediationId("rem1");
        remediationResult.setRemediationResult("success");

        // mocks
        when(client.fetchAppliances(null))
                .thenReturn(CompletableFuture.completedFuture(page));
        when(client.drain("appliance1"))
                .thenReturn(CompletableFuture.completedFuture(drainResult));
        when(client.remediate("appliance1"))
                .thenReturn(CompletableFuture.completedFuture(remediationResult));

        // Run the job synchronously
        processor.fetchAndProcessAllAppliances().join();

        // Verify drain and remediate called
        verify(client).drain("appliance1");
        verify(client).remediate("appliance1");
    }

    @Test
    void testDrainAndRemidiateSingleAppliance_handlesDrainFailure() {
        String applianceId = "appliance2";

        CompletableFuture<DrainResult> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Drain failed"));

        // mocks
        when(client.drain(applianceId)).thenReturn(failedFuture);

        CompletableFuture<Void> result = processor.drainAndRemediateSingleAppliance(applianceId);

        result.join();

        // Verify remediation never called due to drain failure
        verify(client, never()).remediate(anyString());

        // Verify failure log saved
        ArgumentCaptor<ApplianceLog> logCaptor = ArgumentCaptor.forClass(ApplianceLog.class);
        verify(logRepo).save(logCaptor.capture());

        ApplianceLog savedLog = logCaptor.getValue();
        assertEquals("PROCESS", savedLog.getAction());
        assertFalse(savedLog.isSuccess());
        assertTrue(savedLog.getMessage().contains("Drain failed"));
    }

    @Test
    void testFilteringAppliancesToDrain() {
        // Setup
        Appliance applianceLiveNullLastHeard = new Appliance();
        applianceLiveNullLastHeard.setId("appliance1");
        applianceLiveNullLastHeard.setOpStatus("LIVE");
        applianceLiveNullLastHeard.setLastHeardFromOn(null);

        Appliance applianceLiveOldLastHeard = new Appliance();
        applianceLiveOldLastHeard.setId("appliance2");
        applianceLiveOldLastHeard.setOpStatus("LIVE");
        applianceLiveOldLastHeard.setLastHeardFromOn(Instant.now().minus(11, ChronoUnit.MINUTES)); // older than 10 mins

        Appliance applianceLiveRecentLastHeard = new Appliance();
        applianceLiveRecentLastHeard.setId("appliance3");
        applianceLiveRecentLastHeard.setOpStatus("LIVE");
        applianceLiveRecentLastHeard.setLastHeardFromOn(Instant.now().minus(5, ChronoUnit.MINUTES)); // recent, less than 10 mins

        Appliance applianceNonLive = new Appliance();
        applianceNonLive.setId("appliance4");
        applianceNonLive.setOpStatus("OFFLINE");
        applianceNonLive.setLastHeardFromOn(null);

        List<Appliance> applianceList = Arrays.asList(
                applianceLiveNullLastHeard,
                applianceLiveOldLastHeard,
                applianceLiveRecentLastHeard,
                applianceNonLive
        );

        List<Appliance> filtered = processor.getFilteredAppliances(applianceList);

        // Assert only appliances with opStatus LIVE and lastHeardFromOn == null or older than 10 minutes are included
        assertEquals(2, filtered.size());
    }
}
