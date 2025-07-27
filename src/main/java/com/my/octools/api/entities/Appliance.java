package com.my.octools.api.entities;

import lombok.Data;

import java.time.Instant;

@Data
public class Appliance {
    private String id;
    private int sortIndex;
    private String opStatus;
    private Instant lastHeardFromOn;
}
