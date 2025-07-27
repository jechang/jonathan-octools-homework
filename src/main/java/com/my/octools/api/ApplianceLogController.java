package com.my.octools.api;

import com.my.octools.storage.ApplianceLog;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

/**
 * REST controller for appliance log APIs.
 */
@RestController
@RequestMapping("/api/logs")
public class ApplianceLogController {

    private final ApplianceLogService logService;

    public ApplianceLogController(ApplianceLogService logService) {
        this.logService = logService;
    }

    @GetMapping
    public List<ApplianceLog> getAllLogs(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "100") int count) {
        return logService.getAllLogs(start, count);
    }

    @GetMapping("/appliance/{id}")
    public List<ApplianceLog> getLogsForAppliance(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "100") int count) {
        return logService.getLogsByApplianceId(id, start, count);
    }

    @GetMapping("/recent")
    public List<ApplianceLog> getRecentLogs(
            @RequestParam(defaultValue = "5") int minutes,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "100") int count) {
        return logService.getRecentLogs(Duration.ofMinutes(minutes), start, count);
    }

    @GetMapping("/failures")
    public List<ApplianceLog> getFailedLogs(
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "100") int count) {
        return logService.getFailedLogs(start, count);
    }
}
