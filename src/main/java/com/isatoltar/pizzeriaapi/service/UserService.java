package com.isatoltar.pizzeriaapi.service;

import com.isatoltar.pizzeriaapi.converter.RegisterDtoConverter;
import com.isatoltar.pizzeriaapi.dto.AuthRequest;
import com.isatoltar.pizzeriaapi.dto.AuthResponse;
import com.isatoltar.pizzeriaapi.dto.RegisterRequest;
import com.isatoltar.pizzeriaapi.dto.RegisterResponse;
import com.isatoltar.pizzeriaapi.exception.ResourceAlreadyExistsException;
import com.isatoltar.pizzeriaapi.model.User;
import com.isatoltar.pizzeriaapi.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    final UserRepository userRepository;
    final BCryptPasswordEncoder passwordEncoder;
    final RegisterDtoConverter registerDtoConverter;
    final AuthenticationManager authenticationManager;
    final Environment environment;

    public RegisterResponse registerUser(RegisterRequest registerRequest) {

        String username = registerRequest.getUsername();
        User user = getUserByUsername(username).orElse(null);
        if (user != null)
            throw new ResourceAlreadyExistsException(
                    String.format("User with username = %s already exists", username)
            );

        user = User.builder()
                .name(registerRequest.getName())
                .username(username)
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .build();

        return registerDtoConverter.convert(userRepository.save(user));
    }

    public AuthResponse authenticateUser(AuthRequest authRequest) {

        String username = authRequest.getUsername();
        String password = authRequest.getUsername();

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwtSecret = Objects.requireNonNull(environment.getProperty("jwt.secret"));

        String accessToken = Jwts.builder()
                .setSubject((username))
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(2, ChronoUnit.HOURS)))
                .signWith(SignatureAlgorithm.HS512, jwtSecret.getBytes(StandardCharsets.UTF_8))
                .compact();

        return new AuthResponse(accessToken);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
