package com.antigravity.pm.controller;

import com.antigravity.pm.domain.ActivityLog;
import com.antigravity.pm.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/activity")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<List<ActivityLog>> getProjectActivity(@PathVariable Long projectId) {
        return ResponseEntity.ok(activityLogService.getLogsForProject(projectId));
    }
}
