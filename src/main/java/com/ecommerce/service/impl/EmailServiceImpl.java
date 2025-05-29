package com.ecommerce.service.impl;

import com.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Your E-commerce Email Verification OTP");
        message.setText(String.format(
            "Your email verification OTP is: %s\n" +
            "Please use this OTP to verify your email address.",
            otp
        ));

        try {
            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Reset your password");
        message.setText(String.format(
            "Please click the following link to reset your password:\n" +
            "http://localhost:3000/reset-password?token=%s",
            token
        ));

        try {
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    @Override
    public void sendOrderConfirmationEmail(String to, String orderNumber) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Order Confirmation");
        message.setText(String.format(
            "Thank you for your order!\n" +
            "Your order number is: %s\n" +
            "You can track your order status at: http://localhost:3000/orders/%s",
            orderNumber, orderNumber
        ));

        try {
            mailSender.send(message);
            log.info("Order confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to: {}", to, e);
        }
    }

    @Override
    public void sendOrderStatusUpdateEmail(String to, String orderNumber, String status) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Order Status Update");
        message.setText(String.format(
            "Your order status has been updated.\n" +
            "Order number: %s\n" +
            "New status: %s\n" +
            "Track your order at: http://localhost:3000/orders/%s",
            orderNumber, status, orderNumber
        ));

        try {
            mailSender.send(message);
            log.info("Order status update email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send order status update email to: {}", to, e);
        }
    }
} 