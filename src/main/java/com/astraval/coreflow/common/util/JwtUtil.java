package com.astraval.coreflow.common.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600}")
    private int jwtExpiration;

    @Value("${jwt.refresh.expiration:86400}")
    private int refreshExpiration;

    public String generateToken(Long userId, String role, List<Long> companyIds, Long defaultCompanyId, String defaultCompanyName) {
        List<Object> defaultComp = List.of(defaultCompanyId, defaultCompanyName);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role)
                .claim("companyIds", companyIds)
                .claim("defaultComp",defaultComp)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000L))
                .setId(UUID.randomUUID().toString())
                .signWith(Keys.hmacShaKeyFor(getKeyBytes()))
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration * 1000L))
                .setId(UUID.randomUUID().toString())
                .signWith(Keys.hmacShaKeyFor(getKeyBytes()))
                .compact();
    }

    private byte[] getKeyBytes() {
        byte[] keyBytes = jwtSecret.getBytes();
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            return paddedKey;
        }
        return keyBytes;
    }
}