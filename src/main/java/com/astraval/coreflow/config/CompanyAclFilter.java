package com.astraval.coreflow.config;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CompanyAclFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Long requestedCompanyId = extractCompanyId(request.getRequestURI());
        if (requestedCompanyId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        Object details = authentication.getDetails();
        if (!(details instanceof Claims claims)) {
            writeForbidden(response, "Unauthorized company access");
            return;
        }

        Object companyIdsObj = claims.get("companyIds");
        if (!(companyIdsObj instanceof List<?> companyIds)) {
            writeForbidden(response, "Unauthorized company access");
            return;
        }

        Set<Long> allowedCompanyIds = companyIds.stream()
                .map(this::toLong)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (!allowedCompanyIds.contains(requestedCompanyId)) {
            writeForbidden(response, "Unauthorized company access for company ID: " + requestedCompanyId);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Long extractCompanyId(String requestUri) {
        if (requestUri == null || !requestUri.startsWith("/api/companies/")) {
            return null;
        }
        String[] parts = requestUri.split("/");
        if (parts.length < 4) {
            return null;
        }
        try {
            return Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof String strValue) {
            try {
                return Long.parseLong(strValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write("""
            {
              "error": "access_denied",
              "message": "%s"
            }
            """.formatted(message));
    }
}
