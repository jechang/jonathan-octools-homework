package com.my.octools.storage;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * In-memory thread-safe store for appliance logs using ConcurrentSkipListSet
 * sorted by timestamp descending.
 */
@Component
public class InMemoryApplianceLogStore implements ApplianceLogRepository {

    // Comparator sorts by timestamp descending (newest first)
    private final ConcurrentSkipListSet<ApplianceLog> logs = new ConcurrentSkipListSet<>(
            Comparator.comparing(ApplianceLog::getTimestamp).reversed()
                    // Tie-breaker: in case timestamps equal, compare by ID or hashcode to keep consistent order
                    .thenComparing(ApplianceLog::getApplianceId)
                    .thenComparingInt(Object::hashCode)
    );

    /**
     * Retrieves all logs, sorted by timestamp descending.
     */
    @Override
    public List<ApplianceLog> findAll() {
        return new ArrayList<>(logs);
    }

    /**
     * Saves a new log entry, automatically placed in correct order.
     */
    @Override
    public void save(ApplianceLog log) {
        logs.add(log);
    }

    /**
     * Finds logs by appliance ID.
     */
    @Override
    public List<ApplianceLog> findByApplianceId(String applianceId) {
        return logs.stream()
                .filter(log -> log.getApplianceId().equals(applianceId))
                .collect(Collectors.toList());
    }

    /**
     * Finds logs with timestamp after the specified instant.
     */
    @Override
    public List<ApplianceLog> findByTimestampAfter(Instant after) {
        return logs.stream()
                .filter(log -> log.getTimestamp().isAfter(after))
                .collect(Collectors.toList());
    }

    /**
     * Finds all logs with success == false.
     */
    @Override
    public List<ApplianceLog> findBySuccessFalse() {
        return logs.stream()
                .filter(log -> !log.isSuccess())
                .collect(Collectors.toList());
    }
}
