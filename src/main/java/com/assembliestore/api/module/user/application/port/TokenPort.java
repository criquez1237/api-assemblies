package com.assembliestore.api.module.user.application.port;

import com.assembliestore.api.module.user.application.dto.JwtTokenDto;
import com.assembliestore.api.module.user.application.dto.UserDto;

public interface TokenPort {

    void updateToken(JwtTokenDto jwtTokenDto);

    String getUserNameFromToken(final String token);

    JwtTokenDto findByToken(final String token);

    boolean validateToken(String token, UserDto user);
}
