package com.astraval.coreflow.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        log.info("🔍 JWT Filter START - Path: {}, Method: {}", path, request.getMethod());
        
        // ✅ FIX 1: SKIP PUBLIC ENDPOINTS
        if (path.startsWith("/api/auth/") || path.startsWith("/api/test/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = getTokenFromRequest(request);
        
        // ✅ FIX 2: SKIP IF NO TOKEN
        if (token == null) {
            log.warn("JWT Filter - No token found for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        
        log.info("JWT Filter - Processing token for path: {}, token: {}...", path, token.substring(0, Math.min(20, token.length())));
        
        try {
            if (jwtSecret == null || jwtSecret.isEmpty()) {
                log.error("JWT secret is null or empty");
                filterChain.doFilter(request, response);
                return;
            }
            
            byte[] keyBytes = jwtSecret.getBytes();
            if (keyBytes.length < 32) {
                byte[] paddedKey = new byte[32];
                System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
                keyBytes = paddedKey;
            }
            
            Claims claims;
            try {
                var signingKey = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
                if (signingKey == null) {
                    log.warn("JWT Filter - Signing key creation failed");
                    filterChain.doFilter(request, response);
                    return;
                }
                
                claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            } catch (Exception parseException) {
                log.warn("JWT Filter - Token parsing failed: {}", parseException.getMessage(), parseException);
                filterChain.doFilter(request, response);
                return;
            }
            
            if (claims == null) {
                log.warn("JWT Filter - Claims are null");
                filterChain.doFilter(request, response);
                return;
            }
            
            String userId = claims.getSubject();
            if (userId == null) {
                log.warn("JWT Filter - No subject in token");
                filterChain.doFilter(request, response);
                return;
            }
            
            String roleCode = claims.get("role", String.class);
            
            // ✅ FIX 3: HANDLE NULL roleCode
            if (roleCode == null) {
                log.warn("JWT Filter - No roleCode in token for user: {}", userId);
                filterChain.doFilter(request, response);
                return;
            }
            
            List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + roleCode.toUpperCase())  // "ROLE_ADMIN"
            );
            
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authentication.setDetails(claims);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("JWT Filter - ✅ Authenticated user: {} with role: ROLE_{}", userId, roleCode.toUpperCase());
            
        } catch (Exception e) {
            log.warn("JWT Filter - Invalid token: {}", e.getMessage(), e);
            // Continue without authentication (will hit .authenticated() rules)
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
