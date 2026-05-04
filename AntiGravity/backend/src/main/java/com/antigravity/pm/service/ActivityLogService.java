package com.antigravity.pm.service;

import com.antigravity.pm.domain.ActivityLog;
import com.antigravity.pm.domain.Project;
import com.antigravity.pm.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @SuppressWarnings("null")
    public void logActivity(Project project, String message) {
        ActivityLog log = new ActivityLog();
        log.setProject(project);
        log.setMessage(message);
        activityLogRepository.save(log);

        // Broadcast to WebSocket clients subscribed to this project
        messagingTemplate.convertAndSend("/topic/project/" + project.getId() + "/activity", message);
    }
    
    public List<ActivityLog> getLogsForProject(Long projectId) {
        return activityLogRepository.findByProjectIdOrderByTimestampDesc(projectId);
    }
}
