package com.soar_be.auth.service;

import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long EXPIRATION;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateToken(String email, Long userId) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                // Role 생성시 .claim("role", role) 추가
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (SecurityException | MalformedJwtException exception) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException exception) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (Exception exception) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

}
