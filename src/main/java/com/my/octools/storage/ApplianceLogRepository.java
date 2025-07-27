package com.my.octools.storage;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for managing ApplianceLog entries.
 */
public interface ApplianceLogRepository {

    /**
     * Saves a new ApplianceLog entry.
     *
     * @param log the ApplianceLog to save
     */
    void save(ApplianceLog log);

    /**
     * Retrieves all ApplianceLog entries.
     *
     * @return list of all ApplianceLog entries
     */
    List<ApplianceLog> findAll();

    /**
     * Finds logs by appliance ID.
     *
     * @param applianceId the ID of the appliance
     * @return list of ApplianceLog entries for the given appliance ID
     */
    List<ApplianceLog> findByApplianceId(String applianceId);

    /**
     * Finds logs with timestamp after the specified instant.
     *
     * @param after the cutoff Instant timestamp
     * @return list of ApplianceLog entries after the given timestamp
     */
    List<ApplianceLog> findByTimestampAfter(Instant after);

    /**
     * Finds all logs where success is false.
     *
     * @return list of failed ApplianceLog entries
     */
    List<ApplianceLog> findBySuccessFalse();
}
