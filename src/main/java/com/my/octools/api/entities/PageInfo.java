package com.my.octools.api.entities;

import lombok.Data;

@Data
public class PageInfo {
    private boolean hasNextPage;
    private String endCursor;
    private int totalCount;
}