package com.antigravity.pm.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentInfo {
    private Long id;
    private Long taskId;
    private String authorName;
    private String text;
    private String attachmentUrl;
    private LocalDateTime createdAt;
}
