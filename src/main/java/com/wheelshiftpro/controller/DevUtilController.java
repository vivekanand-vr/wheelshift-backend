package com.wheelshiftpro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility controller for development/testing purposes.
 * Remove in production!
 */
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class DevUtilController {

    private final PasswordEncoder passwordEncoder;

    /**
     * Hash a plaintext password using BCrypt.
     * Use this to generate password hashes for seed data.
     */
    @PostMapping("/hash-password")
    public Map<String, String> hashPassword(@RequestBody Map<String, String> request) {
        String plainPassword = request.get("password");
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        Map<String, String> response = new HashMap<>();
        response.put("plaintext", plainPassword);
        response.put("bcryptHash", hashedPassword);
        response.put("algorithm", "BCrypt");
        
        return response;
    }

    /**
     * Verify if a plaintext password matches a BCrypt hash.
     */
    @PostMapping("/verify-password")
    public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request) {
        String plainPassword = request.get("password");
        String hash = request.get("hash");
        
        boolean matches = passwordEncoder.matches(plainPassword, hash);
        
        Map<String, Object> response = new HashMap<>();
        response.put("password", plainPassword);
        response.put("hash", hash);
        response.put("matches", matches);
        
        return response;
    }
}
