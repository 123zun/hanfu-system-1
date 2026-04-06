package com.server.article.dto;

import lombok.Data;
import java.util.List;

@Data
public class ArticlePageDTO {
    private List<ArticleDTO> records;
    private long total;
    private long current;
    private long size;
    private long pages;

    public ArticlePageDTO() {}

    public ArticlePageDTO(List<ArticleDTO> records, long total, long current, long size, long pages) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = pages;
    }
}
