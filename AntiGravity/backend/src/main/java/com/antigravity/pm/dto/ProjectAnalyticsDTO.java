package com.antigravity.pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ProjectAnalyticsDTO {
    private Double completionPercentage;
    private List<TaskInfo> overdueTasks;
}


