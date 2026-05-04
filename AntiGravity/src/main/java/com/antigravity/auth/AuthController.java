package com.antigravity.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles POST /api/auth/login
 *
 * @CrossOrigin allows your frontend running on port 5500 (VS Code Live Server)
 * to call this backend on port 8080 without "Failed to fetch" CORS errors.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    /**
     * Hardcoded user store — no database needed.
     * Key   = username
     * Value = [password, role]
     *
     * To add more users, just add another entry here.
     */
    private static final Map<String, String[]> USERS = new HashMap<>();

    static {
        USERS.put("admin",  new String[]{"admin123",  "ADMIN"});
        USERS.put("member", new String[]{"member123", "MEMBER"});
        USERS.put("test",   new String[]{"test",      "MEMBER"});
    }

    /**
     * POST /api/auth/login
     *
     * Accepts:  { "username": "admin", "password": "admin123" }
     * Returns:  { "success": true, "message": "Login successful", "role": "ADMIN" }
     *       or: { "success": false, "message": "Invalid credentials", "role": null }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        // Basic null / empty check
        if (request.getUsername() == null || request.getPassword() == null
                || request.getUsername().isBlank() || request.getPassword().isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(new LoginResponse(false, "Username and password are required", null));
        }

        String username = request.getUsername().trim().toLowerCase();
        String[] credentials = USERS.get(username);

        // Check user exists and password matches
        if (credentials != null && credentials[0].equals(request.getPassword())) {
            return ResponseEntity.ok(
                    new LoginResponse(true, "Login successful", credentials[1])
            );
        }

        // Wrong username or password — return 401 Unauthorized
        return ResponseEntity
                .status(401)
                .body(new LoginResponse(false, "Invalid username or password", null));
    }

    /**
     * GET /api/auth/ping  — quick health-check so you can verify the backend is running.
     * Hit this in your browser: http://localhost:8080/api/auth/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("AntiGravity backend is running!");
    }
}
