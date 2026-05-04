package com.antigravity.pm.service;

import com.antigravity.pm.domain.Project;
import com.antigravity.pm.domain.Task;
import com.antigravity.pm.domain.TaskStatus;
import com.antigravity.pm.dto.TaskInfo;
import com.antigravity.pm.exception.ResourceNotFoundException;
import com.antigravity.pm.repository.ProjectRepository;
import com.antigravity.pm.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    public List<TaskInfo> getTasksByProject(Long projectId) {
        Project project = projectRepository.findById(Objects.requireNonNull(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return project.getTasks().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<TaskInfo> getAllTasks() {
        return taskRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    public TaskInfo createTask(Long projectId, TaskInfo dto) {
        Project project = projectRepository.findById(Objects.requireNonNull(projectId))
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : TaskStatus.TODO);
        task.setPriority(dto.getPriority());
        task.setTags(dto.getTags());
        task.setDueDate(dto.getDueDate());
        task.setProject(project);
        Task saved = taskRepository.save(task);
        
        TaskInfo response = mapToDTO(saved);
        
        // Log Activity
        activityLogService.logActivity(project, "Task '" + task.getTitle() + "' was created.");
        
        // Send email notification (async)
        notificationService.sendEmailNotification(
            "team@TaskLane.com", 
            "New Task Assigned", 
            "A new task '" + task.getTitle() + "' has been added to project " + project.getName()
        );
        
        // Broadcast WebSocket message
        messagingTemplate.convertAndSend("/topic/tasks", response);
        
        return response;
    }

    @SuppressWarnings("null")
    public TaskInfo updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(Objects.requireNonNull(taskId))
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(status);
        Task saved = taskRepository.save(task);
        
        TaskInfo response = mapToDTO(saved);
        
        // Log Activity
        activityLogService.logActivity(task.getProject(), "Task '" + task.getTitle() + "' was moved from " + oldStatus + " to " + status);
        
        // Broadcast WebSocket message for Live Update
        messagingTemplate.convertAndSend("/topic/tasks", response);
        
        return response;
    }

    private TaskInfo mapToDTO(Task task) {
        return new TaskInfo(task.getId(), task.getTitle(), task.getStatus(), task.getPriority(), task.getTags(), task.getDueDate());
    }
}
