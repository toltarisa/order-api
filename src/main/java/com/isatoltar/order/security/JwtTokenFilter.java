package com.isatoltar.order.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtTokenFilter extends OncePerRequestFilter {

    final Environment environment;

    public JwtTokenFilter(Environment environment) {
        this.environment = environment;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.split("Bearer ")[1];

                String jwtSecret = Objects.requireNonNull(environment.getProperty("jwt.secret"));
                byte[] secret = jwtSecret.getBytes(StandardCharsets.UTF_8);

                String userName = Jwts.parser()
                        .setSigningKey(secret)
                        .parseClaimsJws(token)
                        .getBody().getSubject();

                List<String> privileges = (List<String>) Jwts.parser()
                        .setSigningKey(secret)
                        .parseClaimsJws(token)
                        .getBody().get("privileges");

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userName,
                        null,
                        privileges.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } catch (Exception ex) {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                new ObjectMapper().writeValue(response.getOutputStream(), Map.of("message", ex.getMessage()));
            }
        } else
            filterChain.doFilter(request, response);
    }
}