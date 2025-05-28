package com.ecommerce.domain.repository;

import com.ecommerce.domain.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenAndTypeAndUsedFalse(String token, Token.TokenType type);
    void deleteByUser_IdAndType(Long userId, Token.TokenType type);
} 