package com.ricardocervo.booknblock.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;


    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        String uri = request.getRequestURI();
        if (!request.getRequestURI().endsWith("/authenticate") && !request.getRequestURI().startsWith("/h2-console/")) {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                handleErrorResponse(response, "Not logged in: authorization header with authorization token is missing");
                return;
            }
        } else {
            filterChain.doFilter(request, response);
            return;
        }



        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractUserName(jwt);
        } catch (ExpiredJwtException e) {
            handleErrorResponse(response, "Not logged in: " + e.getMessage());
            return;
        } catch (Exception e) {
            handleErrorResponse(response, e.getMessage());
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleErrorResponse(HttpServletResponse response, String errorMsg) throws IOException {
        int httpStatus = UNAUTHORIZED.value();
        String httpError = HttpStatus.UNAUTHORIZED.getReasonPhrase();
        log.error("Error logging in: {}", errorMsg);
        response.setHeader("error", errorMsg);
        response.setStatus(httpStatus);

        Map<String, String> error = new LinkedHashMap<>();
        error.put("httpStatus", String.valueOf(httpStatus));
        error.put("httpError", httpError);

        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"));

        error.put("timestamp", formattedDateTime);
        error.put("message", errorMsg);

        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}
