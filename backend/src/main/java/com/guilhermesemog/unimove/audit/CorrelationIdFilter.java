package com.guilhermesemog.unimove.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        UUID correlationId = parseOrCreate(request.getHeader(HEADER_NAME));
        CorrelationIdContext.set(correlationId);
        response.setHeader(HEADER_NAME, correlationId.toString());

        try {
            filterChain.doFilter(request, response);
        } finally {
            CorrelationIdContext.clear();
        }
    }

    private UUID parseOrCreate(String value) {
        if (value == null || value.isBlank()) {
            return UUID.randomUUID();
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return UUID.randomUUID();
        }
    }
}
