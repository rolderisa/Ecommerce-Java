package com.ecommerce.service.impl;

import com.ecommerce.domain.model.Address;
import com.ecommerce.domain.model.Token;
import com.ecommerce.domain.model.User;
import com.ecommerce.domain.repository.TokenRepository;
import com.ecommerce.domain.repository.UserRepository;
import com.ecommerce.dto.auth.LoginRequest;
import com.ecommerce.dto.auth.LoginResponse;
import com.ecommerce.dto.auth.RegisterRequest;
import com.ecommerce.dto.auth.RegisterResponse;
import com.ecommerce.exception.EmailAlreadyExistsException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.TokenExpiredException;
import com.ecommerce.security.JwtTokenProvider;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    private static final int EMAIL_VERIFICATION_EXPIRY_HOURS = 24;
    private static final int PASSWORD_RESET_EXPIRY_HOURS = 1;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);

        Address address = new Address();
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setZipCode(request.getZipCode());
        address.setUser(user);
        address.setDefault(true);

        user.setAddress(address);
        userRepository.save(user);

        // Create and save verification token
        String token = UUID.randomUUID().toString();
        Token verificationToken = new Token();
        verificationToken.setToken(token);
        verificationToken.setType(Token.TokenType.EMAIL_VERIFICATION);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(EMAIL_VERIFICATION_EXPIRY_HOURS));
        tokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), token);

        return new RegisterResponse(
            "Registration successful. Please check your email to verify your account.",
            user.getEmail()
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new LoginResponse(
            token,
            user.getEmail(),
            user.getRoles().stream().toList()
        );
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        Token verificationToken = tokenRepository.findByTokenAndTypeAndUsedFalse(token, Token.TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new TokenExpiredException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Delete any existing reset tokens for this user
        tokenRepository.deleteByUser_IdAndType(user.getId(), Token.TokenType.PASSWORD_RESET);

        // Create and save new reset token
        String token = UUID.randomUUID().toString();
        Token resetToken = new Token();
        resetToken.setToken(token);
        resetToken.setType(Token.TokenType.PASSWORD_RESET);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(PASSWORD_RESET_EXPIRY_HOURS));
        tokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(email, token);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Token resetToken = tokenRepository.findByTokenAndTypeAndUsedFalse(token, Token.TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));

        if (resetToken.isExpired()) {
            throw new TokenExpiredException("Reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
} 