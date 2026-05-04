package com.antigravity.pm.controller;

import com.antigravity.pm.dto.CommentInfo;
import com.antigravity.pm.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentInfo>> getComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getCommentsByTask(taskId));
    }

    @PostMapping
    public ResponseEntity<CommentInfo> addComment(
            @PathVariable Long taskId,
            @RequestParam("text") String text,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication) {
        
        String attachmentUrl = null;
        if (file != null && !file.isEmpty()) {
            // Mocking file upload for now (would save to S3 or local disk in production)
            attachmentUrl = "/uploads/" + file.getOriginalFilename();
        }

        String username = authentication.getName();
        return ResponseEntity.ok(commentService.addComment(taskId, username, text, attachmentUrl));
    }
}
