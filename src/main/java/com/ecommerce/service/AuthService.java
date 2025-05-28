package com.ecommerce.service;

import com.ecommerce.dto.auth.LoginRequest;
import com.ecommerce.dto.auth.LoginResponse;
import com.ecommerce.dto.auth.RegisterRequest;
import com.ecommerce.dto.auth.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    void verifyEmail(String token);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
} 