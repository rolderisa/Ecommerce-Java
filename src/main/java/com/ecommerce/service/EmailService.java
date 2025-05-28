package com.ecommerce.service;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);
    void sendOrderConfirmationEmail(String to, String orderNumber);
    void sendOrderStatusUpdateEmail(String to, String orderNumber, String status);
} 