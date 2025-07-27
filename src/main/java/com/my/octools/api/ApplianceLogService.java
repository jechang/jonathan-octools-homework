package com.my.octools.api;

import com.my.octools.storage.ApplianceLog;
import com.my.octools.storage.InMemoryApplianceLogStore;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class ApplianceLogService {
    private final InMemoryApplianceLogStore store;

    public ApplianceLogService(InMemoryApplianceLogStore store) {
        this.store = store;
    }

    public List<ApplianceLog> getAllLogs(int start, int count) {
        return store.findAll().stream()
                .sorted(Comparator.comparing(ApplianceLog::getTimestamp).reversed())
                .skip(start)
                .limit(count)
                .toList();
    }

    public List<ApplianceLog> getLogsByApplianceId(String id, int start, int count) {
        return store.findByApplianceId(id).stream()
                .sorted(Comparator.comparing(ApplianceLog::getTimestamp).reversed())
                .skip(start)
                .limit(count)
                .toList();
    }

    public List<ApplianceLog> getRecentLogs(Duration duration, int start, int count) {
        Instant cutoff = Instant.now().minus(duration);
        return store.findByTimestampAfter(cutoff).stream()
                .sorted(Comparator.comparing(ApplianceLog::getTimestamp).reversed())
                .skip(start)
                .limit(count)
                .toList();
    }

    public List<ApplianceLog> getFailedLogs(int start, int count) {
        return store.findBySuccessFalse().stream()
                .sorted(Comparator.comparing(ApplianceLog::getTimestamp).reversed())
                .skip(start)
                .limit(count)
                .toList();
    }
}
