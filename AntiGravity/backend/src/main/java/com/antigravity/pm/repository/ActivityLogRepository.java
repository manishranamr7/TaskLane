package com.antigravity.pm.repository;

import com.antigravity.pm.domain.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByProjectIdOrderByTimestampDesc(Long projectId);
}
