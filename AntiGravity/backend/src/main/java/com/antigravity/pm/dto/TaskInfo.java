package com.antigravity.pm.dto;

import com.antigravity.pm.domain.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TaskInfo {
    private Long id;
    private String title;
    private TaskStatus status;
    private String priority;
    private String tags;
    private LocalDate dueDate;
}


