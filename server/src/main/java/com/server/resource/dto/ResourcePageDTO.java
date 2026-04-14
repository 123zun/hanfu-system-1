package com.server.resource.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResourcePageDTO {

    private List<ResourceDTO> records;
    private Long total;
    private Integer page;
    private Integer size;
    private Long pages;

    public ResourcePageDTO(List<ResourceDTO> records, Long total, Integer page, Integer size, Long pages) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = pages;
    }
}
