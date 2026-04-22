package com.server.common.dto;

import lombok.Data;

@Data
public class AuditRequest {
    private Boolean approved;   // true-通过, false-不通过
    private String reason;       // 审核不通过原因
    private Long auditorId;      // 审核人ID
}
