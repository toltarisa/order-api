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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "Auth")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class AuthController {

    final AuthService authService;

    @ApiOperation(value = "Register user")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody @Valid RegisterRequest registerRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerUser(registerRequest));
    }

    @ApiOperation(value = "Create an access token")
    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> authenticateUser(@RequestBody @Valid AuthRequest authRequest) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.authenticateUser(authRequest));
    }
}
