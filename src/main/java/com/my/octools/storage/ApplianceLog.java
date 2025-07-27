package com.my.octools.storage;

import java.time.Instant;

/**
 * Entity representing an appliance log entry.
 */
public class ApplianceLog {

    private String applianceId;
    private String action;      // e.g., DRAIN or REMEDIATE
    private boolean success;
    private String message;
    private Instant timestamp;

    public ApplianceLog(String applianceId, String action, boolean success, String message, Instant timestamp) {
        this.applianceId = applianceId;
        this.action = action;
        this.success = success;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getApplianceId() {
        return applianceId;
    }

    public void setApplianceId(String applianceId) {
        this.applianceId = applianceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
