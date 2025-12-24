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


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // ✅ FIX 1: SKIP PUBLIC ENDPOINTS
        if (path.startsWith("/api/auth/") || path.startsWith("/api/test/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = getTokenFromRequest(request);
        
        // ✅ FIX 2: SKIP IF NO TOKEN
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        
        try {
            if (jwtSecret == null || jwtSecret.isEmpty()) {
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
                    filterChain.doFilter(request, response);
                    return;
                }
                
                claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            } catch (Exception parseException) {
                filterChain.doFilter(request, response);
                return;
            }
            
            if (claims == null) {
                filterChain.doFilter(request, response);
                return;
            }
            
            String userId = claims.getSubject();
            if (userId == null) {
                filterChain.doFilter(request, response);
                return;
            }
            
            String roleCode = claims.get("role", String.class);
            
            // ✅ FIX 3: HANDLE NULL roleCode
            if (roleCode == null) {
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
            
        } catch (Exception e) {
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
