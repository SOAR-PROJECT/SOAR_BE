package com.soar_be.auth.service;

import com.soar_be.auth.details.CustomUserDetails;
import com.soar_be.auth.dto.TokenResponse;
import com.soar_be.domain.user.entity.Role;
import com.soar_be.global.exception.CustomException;
import com.soar_be.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    @Value("${jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public TokenResponse generateTokenResponse(String email, Long userId, Role role) {
        Date now = new Date();
        Date accessExpirationDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);
        Date refreshExpirationDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION);

        String accessToken = Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role.name())
                .setIssuedAt(now)
                .setExpiration(accessExpirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(refreshExpirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        long expiresIn = accessExpirationDate.getTime() - now.getTime();

        return TokenResponse.create(accessToken, refreshToken, expiresIn);
    }


    public String generateToken(String email, Long userId, Role role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION);

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role.name())
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

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Role getRoleFromToken(String token) {
        return Role.valueOf(getClaims(token).get("role", String.class));
    }

    public Authentication getAuthentication(String token) {
        String email = getEmailFromToken(token);
        Long userId = getUserIdFromToken(token);
        Role role = getRoleFromToken(token);

        if (email == null || userId == null || role == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Collection<? extends GrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));

        UserDetails userDetails = new CustomUserDetails(userId, email, "", authorities);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                authorities
        );
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}
