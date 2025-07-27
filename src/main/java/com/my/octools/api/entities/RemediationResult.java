package com.my.octools.api.entities;

import lombok.Data;

@Data
public class RemediationResult {
    private String remediationId;
    private String remediationResult;

    @Override
    public String toString() {
        return "RemediationResult{" +
                "remediationId='" + remediationId + '\'' +
                ", remediationResult='" + remediationResult + '\'' +
                '}';
    }
}