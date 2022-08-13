package com.isatoltar.order.controller;

import com.isatoltar.order.dto.AuthRequest;
import com.isatoltar.order.dto.AuthResponse;
import com.isatoltar.order.dto.RegisterRequest;
import com.isatoltar.order.dto.RegisterResponse;
import com.isatoltar.order.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.registerUser(registerRequest));
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody AuthRequest authRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.authenticateUser(authRequest));
    }
}
