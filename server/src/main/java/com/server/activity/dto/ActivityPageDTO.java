package com.server.activity.dto;

import lombok.Data;
import java.util.List;

@Data
public class ActivityPageDTO {
    private List<ActivityDTO> records;
    private long total;
    private long current;
    private long size;
    private long pages;

    public ActivityPageDTO() {}

    public ActivityPageDTO(List<ActivityDTO> records, long total, long current, long size, long pages) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = pages;
    }
}
