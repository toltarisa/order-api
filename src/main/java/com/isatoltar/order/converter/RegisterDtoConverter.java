package com.isatoltar.order.converter;

import com.isatoltar.order.dto.RegisterResponse;
import com.isatoltar.order.model.User;
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
