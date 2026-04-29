package com.ecommerce.shop.config;

import com.ecommerce.shop.dto.response.ApiResponse;
import com.ecommerce.shop.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private record WindowCounter(long windowStartMs, AtomicInteger count) {
    }

    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Value("${app.rate-limit.requests-per-minute:120}")
    private int requestsPerMinute;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/api/auth")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        long minuteWindowStart = now - (now % 60_000);

        WindowCounter current = counters.compute(ip, (key, existing) -> {
            if (existing == null || existing.windowStartMs() != minuteWindowStart) {
                return new WindowCounter(minuteWindowStart, new AtomicInteger(0));
            }
            return existing;
        });

        int currentCount = current.count().incrementAndGet();
        if (currentCount > requestsPerMinute) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            ApiResponse.error(
                                    ErrorCode.RATE_LIMITED,
                                    "Too many requests. Please retry after one minute."
                            )
                    )
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
