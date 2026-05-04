package com.antigravity.pm.controller;

import com.antigravity.pm.domain.User;
import com.antigravity.pm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.antigravity.pm.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(user);
    }

    @PostMapping("/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!file.isEmpty()) {
            // Mocking avatar upload
            String avatarUrl = "/uploads/avatars/" + file.getOriginalFilename();
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
            return ResponseEntity.ok(avatarUrl);
        }
        return ResponseEntity.badRequest().body("File is empty");
    }
}
