package com.my.octools.api.entities;

import lombok.Data;

@Data
public class DrainResult {
    private String drainId;
    private String estimatedTimeToDrain;

    @Override
    public String toString() {
        return "DrainResult{" +
                "drainId='" + drainId + '\'' +
                ", estimatedTimeToDrain='" + estimatedTimeToDrain + '\'' +
                '}';
    }

}
