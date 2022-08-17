package com.isatoltar.order.service;

import com.isatoltar.order.converter.RegisterDtoConverter;
import com.isatoltar.order.dto.RegisterRequest;
import com.isatoltar.order.dto.RegisterResponse;
import com.isatoltar.order.exception.ResourceAlreadyExistsException;
import com.isatoltar.order.model.User;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class AuthServiceTest {

    @Mock UserService userService;
    @Mock RegisterDtoConverter registerDtoConverter;
    @Mock BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock Environment environment;
    @Mock AuthenticationManager authenticationManager;

    AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(
                authenticationManager,
                bCryptPasswordEncoder,
                environment,
                registerDtoConverter,
                userService
        );
    }

    @Test
    @DisplayName("Register method should register user with valid request")
    void itShouldRegisterUserWithValidRequest() {

        // given
        String hashedPassword = "$2a$10$wgQ8HdjQ7.lS.Wm8Bxm6KeNV.JbMoDFowy3zFDhnFkOvEyuhbHFoC";
        RegisterRequest request = new RegisterRequest("Isa Toltar", "isatoltar", "isatoltar");
        RegisterResponse response = new RegisterResponse(1, request.getName(), request.getUsername());
        User user = User.builder()
                .name("Isa Toltar")
                .username("isatoltar")
                .password(hashedPassword)
                .build();

        User createdUser = User.builder()
                .id(1)
                .name("Isa Toltar")
                .username("isatoltar")
                .password(hashedPassword)
                .build();

        // when
        when(userService.getUserByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(bCryptPasswordEncoder.encode("isatoltar")).thenReturn(hashedPassword);
        when(registerDtoConverter.convert(createdUser)).thenReturn(response);
        when(userService.save(user)).thenReturn(createdUser);

        // then
        RegisterResponse registerResponse = authService.registerUser(request);

        Mockito.verify(userService).getUserByUsername(request.getUsername());
        Mockito.verify(bCryptPasswordEncoder).encode("isatoltar");
        Mockito.verify(registerDtoConverter).convert(createdUser);
        Mockito.verify(userService).save(user);

        assertThat(registerResponse.getName()).isEqualTo(response.getName());
        assertThat(registerResponse.getUsername()).isEqualTo(response.getUsername());
        assertThat(registerResponse.getId()).isEqualTo(response.getId());
    }

    @Test
    @DisplayName("Register method should throw resource already exists exception when username already exists")
    void itShouldThrowExceptionWhenGivenUsernameAlreadyExists() {

        // given
        RegisterRequest request = new RegisterRequest("Isa Toltar", "isatoltar", "isatoltar");
        User createdUser = User.builder()
                .id(1)
                .name("Isa Toltar")
                .username("isatoltar")
                .password("$2a$10$wgQ8HdjQ7.lS.Wm8Bxm6KeNV.JbMoDFowy3zFDhnFkOvEyuhbHFoC")
                .build();
        // when
        when(userService.getUserByUsername(request.getUsername())).thenReturn(Optional.of(createdUser));

        // then
        assertThatThrownBy(() -> authService.registerUser(request))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining(String.format("User with username = %s already exists", createdUser.getUsername()));

    }
}