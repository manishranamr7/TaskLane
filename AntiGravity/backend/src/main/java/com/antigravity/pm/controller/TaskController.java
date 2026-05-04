package com.antigravity.pm.controller;

import com.antigravity.pm.domain.TaskStatus;
import com.antigravity.pm.dto.TaskInfo;
import com.antigravity.pm.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskInfo>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskInfo>> getTasksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PostMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<TaskInfo> createTask(@PathVariable Long projectId, @RequestBody TaskInfo TaskInfo) {
        return ResponseEntity.ok(taskService.createTask(projectId, TaskInfo));
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskInfo> updateTaskStatus(@PathVariable Long taskId, @RequestParam TaskStatus status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, status));
    }
}


