package com.antigravity.pm.service;

import com.antigravity.pm.domain.Project;
import com.antigravity.pm.domain.Task;
import com.antigravity.pm.domain.TaskStatus;
import com.antigravity.pm.dto.ProjectAnalyticsDTO;
import com.antigravity.pm.dto.TaskInfo;
import com.antigravity.pm.exception.ResourceNotFoundException;
import com.antigravity.pm.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectAnalyticsService {

    private final ProjectRepository projectRepository;

    public ProjectAnalyticsDTO getProjectAnalytics(Long projectId) {
        Project project = projectRepository.findById(Objects.requireNonNull(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<Task> tasks = project.getTasks();
        int totalTasks = tasks.size();

        if (totalTasks == 0) {
            return new ProjectAnalyticsDTO(0.0, Collections.emptyList());
        }

        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();

        double completionPercentage = (double) completedTasks / totalTasks * 100.0;

        LocalDate today = LocalDate.now();
        List<TaskInfo> overdueTasks = tasks.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE)
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today))
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new ProjectAnalyticsDTO(Math.round(completionPercentage * 100.0) / 100.0, overdueTasks);
    }

    private TaskInfo mapToDTO(Task task) {
        return new TaskInfo(task.getId(), task.getTitle(), task.getStatus(), task.getPriority(), task.getTags(), task.getDueDate());
    }
}


