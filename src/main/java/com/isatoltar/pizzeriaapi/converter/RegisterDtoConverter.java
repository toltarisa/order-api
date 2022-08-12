package com.isatoltar.pizzeriaapi.converter;

import com.isatoltar.pizzeriaapi.dto.RegisterResponse;
import com.isatoltar.pizzeriaapi.model.User;
import org.springframework.stereotype.Component;

@Component
public class RegisterDtoConverter {

    public RegisterResponse convert(User user) {
        return new RegisterResponse(
                user.getId(),
                user.getName(),
                user.getUsername()
        );
    }

}
