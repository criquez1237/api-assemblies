package com.assembliestore.api.module.user.application.port;

import com.assembliestore.api.module.user.application.command.RegisterCommand;
import com.assembliestore.api.module.user.application.command.AuthenticationCommand;
import com.assembliestore.api.module.user.application.dto.JwtTokenDto;

public interface AuthPort {

    JwtTokenDto authentication(AuthenticationCommand command);
    
    JwtTokenDto register(RegisterCommand command);

    JwtTokenDto registerWithOTP(RegisterCommand command);

    JwtTokenDto verifyOTPAndActivate(String email, String otpCode);

    JwtTokenDto refreshToken(String refreshToken);

    JwtTokenDto logout(String token);

    
}
