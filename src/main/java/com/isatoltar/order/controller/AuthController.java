package com.isatoltar.order.controller;

import com.isatoltar.order.dto.AuthRequest;
import com.isatoltar.order.dto.AuthResponse;
import com.isatoltar.order.dto.RegisterRequest;
import com.isatoltar.order.dto.RegisterResponse;
import com.isatoltar.order.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Auth")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    final AuthService authService;

    @ApiOperation(value = "Register user")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerUser(registerRequest));
    }

    @ApiOperation(value = "Create an access token")
    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest authRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.authenticateUser(authRequest));
    }
}
