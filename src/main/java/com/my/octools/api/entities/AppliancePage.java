package com.my.octools.api.entities;

import lombok.Data;

import java.util.List;

@Data
public class AppliancePage {
    private PageInfo pageInfo;
    private List<Appliance> data;
}
