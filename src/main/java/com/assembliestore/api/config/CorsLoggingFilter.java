package com.assembliestore.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CorsLoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CorsLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        boolean isPreflight = "OPTIONS".equalsIgnoreCase(method);

        filterChain.doFilter(request, response);

        String allowOrigin = response.getHeader("Access-Control-Allow-Origin");
        boolean corsRejected = origin != null && allowOrigin == null;

        logger.info("[CORS] {} {} | Origin: {} | Preflight: {} | Allow-Origin: {}{}",
                method, uri, origin, isPreflight, allowOrigin,
                corsRejected ? " | RECHAZADA POR CORS" : "");
    }
}
