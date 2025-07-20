package com.assembliestore.api.module.user.infrastructure.adapter.mapper;

import com.assembliestore.api.module.user.application.command.AuthenticationCommand;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignInRequest;

public class SignInMapper {

    public static AuthenticationCommand toSignAuthenticationCommand(SignInRequest loginRequest) {

        return new AuthenticationCommand(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
    }

}
