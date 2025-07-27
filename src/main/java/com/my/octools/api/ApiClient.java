package com.my.octools.api;

import com.my.octools.api.entities.AppliancePage;
import com.my.octools.api.entities.DrainResult;
import com.my.octools.api.entities.RemediationResult;

import java.util.concurrent.CompletableFuture;

/**
 * Client interface for interacting with remote appliance APIs.
 */
public interface ApiClient {
    /**
     * Fetches a page of appliances starting after a given cursor.
     *
     * @param after cursor to fetch after (nullable)
     * @return CompletableFuture with the appliance page data
     */
    CompletableFuture<AppliancePage> fetchAppliances(String after);

    /**
     * Drains the appliance identified by the given ID.
     *
     * @param id appliance identifier
     * @return CompletableFuture with drain result
     */
    CompletableFuture<DrainResult> drain(String id);

    /**
     * Remediates the appliance identified by the given ID.
     *
     * @param id appliance identifier
     * @return CompletableFuture with remediation result
     */
    CompletableFuture<RemediationResult> remediate(String id);
}
