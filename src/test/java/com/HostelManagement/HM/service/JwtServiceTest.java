package com.HostelManagement.HM.service;

import com.HostelManagement.HM.model.Users;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void generateTokenSuccess() {
        JwtService jwtService = new JwtService();
        String key = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        ReflectionTestUtils.setField(jwtService, "secretKey", key);

        Users user = new Users();
        user.setUserId(100L);
        user.setUserName("ramesh");

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractUserIdSuccess() {
        JwtService jwtService = new JwtService();
        String key = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        ReflectionTestUtils.setField(jwtService, "secretKey", key);

        Users user = new Users();
        user.setUserId(100L);
        user.setUserName("ramesh");
        String token = jwtService.generateToken(user);

        long result = jwtService.extractUserId(token);

        assertEquals(100L, result);
    }

    @Test
    void validateTokenSuccess() {
        JwtService jwtService = new JwtService();
        String key = Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes());
        ReflectionTestUtils.setField(jwtService, "secretKey", key);

        Users user = new Users();
        user.setUserId(100L);
        user.setUserName("ramesh");
        String token = jwtService.generateToken(user);

        boolean result = jwtService.validateToken(token, null);

        assertTrue(result);
    }
}
