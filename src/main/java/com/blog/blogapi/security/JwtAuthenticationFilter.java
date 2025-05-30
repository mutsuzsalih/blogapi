package com.blog.blogapi.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger filterLogger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            filterLogger.warn("JWT token is expired: {} for URI: {}", e.getMessage(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        } catch (UnsupportedJwtException e) {
            filterLogger.warn("Unsupported JWT token: {} for URI: {}", e.getMessage(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        } catch (MalformedJwtException e) {
            filterLogger.warn("Malformed JWT token: {} for URI: {}", e.getMessage(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        } catch (SignatureException e) {
            filterLogger.warn("JWT signature validation failed: {} for URI: {}", e.getMessage(),
                    request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        } catch (IllegalArgumentException e) {
            filterLogger.warn("Invalid JWT token: {} for URI: {}", e.getMessage(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            filterLogger.error("Unexpected error extracting username from JWT: {} for URI: {}", e.getMessage(),
                    request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                filterLogger.warn("User not found for JWT: {}, username: {}", e.getMessage(), username);
                filterChain.doFilter(request, response);
                return;
            } catch (Exception e) {
                filterLogger.error("Error loading UserDetails for username {}: {}", username, e.getMessage());
                filterChain.doFilter(request, response);
                return;
            }

            try {
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    filterLogger.warn("JWT is NOT valid (after user found) for user: {} for URI: {}", username,
                            request.getRequestURI());
                }
            } catch (Exception e) {
                filterLogger.error("Error during final token validation or setting SecurityContext for user {}: {}",
                        username, e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}