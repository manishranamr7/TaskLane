package com.antigravity.pm.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final JavaMailSender mailSender;
    private final com.antigravity.pm.repository.NotificationRepository notificationRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Async
    public void sendEmailNotification(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("Email sent asynchronously to {}", to);
        } catch(Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    public void createNotification(com.antigravity.pm.domain.User recipient, String message) {
        com.antigravity.pm.domain.Notification notification = new com.antigravity.pm.domain.Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notificationRepository.save(notification);

        // Push via WebSocket
        messagingTemplate.convertAndSendToUser(recipient.getUsername(), "/queue/notifications", message);
    }
}
