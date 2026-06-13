package com.project.project.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.project.project.entity.UserAccount;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;

/**
 * Generates and validates JWT access tokens for authenticated users.
 */
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey secretKey;
    private final JwtParser parser;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parser().verifyWith(secretKey).build();
    }

    public String generateAccessToken(UserAccount userAccount) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getAccessTokenMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(userAccount.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("role", userAccount.getRole().name())
                .signWith(secretKey)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parser.parseSignedClaims(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public String extractUsername(String token) {
        Claims claims = parser.parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }
}
