package com.antigravity.auth;

/**
 * Represents the JSON body the frontend sends:
 * { "username": "admin", "password": "1234" }
 */
public class LoginRequest {

    private String username;
    private String password;

    // Spring needs a no-args constructor to deserialize JSON
    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
