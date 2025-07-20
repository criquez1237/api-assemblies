package com.assembliestore.api.module.user.infrastructure.adapter.mapper;

import com.assembliestore.api.module.user.application.command.RegisterCommand;
import com.assembliestore.api.module.user.application.dto.JwtTokenDto;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignUpRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.TokenResponse;

public class SignUpMapper {

    public static RegisterCommand toRegisterCommand(SignUpRequest signUpRequest) {

        RegisterCommand command = new RegisterCommand(
                signUpRequest.getEmail(),
                signUpRequest.getPassword(),
                signUpRequest.getNames(),
                signUpRequest.getSurnames(),
                signUpRequest.getImagePerfil(),
                signUpRequest.getPhone());

        return command;
    }

    public static TokenResponse toTokenResponse(JwtTokenDto tokensDto) {

        TokenResponse tokenResponse = new TokenResponse(
                tokensDto.getToken(),
                tokensDto.getRefreshToken());

        return tokenResponse;
    }

}
