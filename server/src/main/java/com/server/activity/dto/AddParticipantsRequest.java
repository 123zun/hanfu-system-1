package com.server.activity.dto;

import lombok.Data;
import java.util.List;

@Data
public class AddParticipantsRequest {
    private Long activityId;
    private List<Long> userIds;     // for add
    private List<Long> signupIds;   // for remove
}
