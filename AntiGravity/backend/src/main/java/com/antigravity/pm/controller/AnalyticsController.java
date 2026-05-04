package com.antigravity.pm.controller;

import com.antigravity.pm.dto.ProjectAnalyticsDTO;
import com.antigravity.pm.service.ProjectAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final ProjectAnalyticsService analyticsService;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ProjectAnalyticsDTO> getProjectAnalytics(@PathVariable Long projectId) {
        return ResponseEntity.ok(analyticsService.getProjectAnalytics(projectId));
    }
}
