package com.antigravity.pm.service;

import com.antigravity.pm.domain.Comment;
import com.antigravity.pm.domain.Task;
import com.antigravity.pm.domain.User;
import com.antigravity.pm.dto.CommentInfo;
import com.antigravity.pm.exception.ResourceNotFoundException;
import com.antigravity.pm.repository.CommentRepository;
import com.antigravity.pm.repository.TaskRepository;
import com.antigravity.pm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<CommentInfo> getCommentsByTask(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public CommentInfo addComment(Long taskId, String username, String text, String attachmentUrl) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setTask(task);
        comment.setAuthor(user);
        comment.setText(text);
        comment.setAttachmentUrl(attachmentUrl);

        Comment saved = commentRepository.save(comment);

        // Notify assignee
        if (task.getAssignee() != null && !task.getAssignee().getUsername().equals(username)) {
            notificationService.createNotification(task.getAssignee(), "New comment on task: " + task.getTitle());
        }

        return mapToDTO(saved);
    }

    private CommentInfo mapToDTO(Comment comment) {
        CommentInfo info = new CommentInfo();
        info.setId(comment.getId());
        info.setTaskId(comment.getTask().getId());
        info.setAuthorName(comment.getAuthor().getUsername());
        info.setText(comment.getText());
        info.setAttachmentUrl(comment.getAttachmentUrl());
        info.setCreatedAt(comment.getCreatedAt());
        return info;
    }
}
