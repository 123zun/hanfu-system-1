package com.server.work.dto;

import lombok.Data;
import java.util.List;

@Data
public class WorkPageDTO {
    private List<WorkDTO> records;
    private long total;
    private long current;
    private long size;
    private long pages;

    public WorkPageDTO() {}

    public WorkPageDTO(List<WorkDTO> records, long total, long current, long size, long pages) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = pages;
    }
}
