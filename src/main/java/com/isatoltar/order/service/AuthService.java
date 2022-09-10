package com.isatoltar.order.service;

import com.isatoltar.order.converter.RegisterDtoConverter;
import com.isatoltar.order.dto.AuthRequest;
import com.isatoltar.order.dto.AuthResponse;
import com.isatoltar.order.dto.RegisterRequest;
import com.isatoltar.order.dto.RegisterResponse;
import com.isatoltar.order.exception.ResourceAlreadyExistsException;
import com.isatoltar.order.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthService {

    final AuthenticationManager authenticationManager;
    final BCryptPasswordEncoder passwordEncoder;
    final Environment environment;

    final RegisterDtoConverter registerDtoConverter;
    final UserService userService;

    public RegisterResponse registerUser(RegisterRequest registerRequest) {

        String username = registerRequest.getUsername();
        userService.getUserByUsername(username).ifPresent((existingUser) -> {
            throw new ResourceAlreadyExistsException(
                    String.format("User with username = %s already exists", existingUser.getUsername())
            );
        });

        User user = User.builder()
                .name(registerRequest.getName())
                .username(username)
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .build();

        return registerDtoConverter.convert(userService.save(user));
    }

    public AuthResponse authenticateUser(AuthRequest authRequest) {

        String username = authRequest.getUsername();
        String password = authRequest.getPassword();

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtSecret = Objects.requireNonNull(environment.getProperty("jwt.secret"));

        String accessToken = Jwts.builder()
                .setSubject((username))
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(2, ChronoUnit.HOURS)))
                .claim("privileges", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).collect(Collectors.toSet()))
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes(StandardCharsets.UTF_8))
                .compact();

        return new AuthResponse(accessToken);
    }
}