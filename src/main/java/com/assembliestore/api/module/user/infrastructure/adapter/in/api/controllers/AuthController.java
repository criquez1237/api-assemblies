package com.assembliestore.api.module.user.infrastructure.adapter.in.api.controllers;

import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignInRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignUpRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.TokenResponse;
import com.assembliestore.api.module.user.infrastructure.adapter.mapper.SignInMapper;
import com.assembliestore.api.module.user.infrastructure.adapter.mapper.SignUpMapper;
import com.assembliestore.api.common.error.CredentialInvalidExeption;
import com.assembliestore.api.module.user.application.dto.JwtTokenDto;
import com.assembliestore.api.module.user.application.port.AuthPort;
import com.assembliestore.api.module.user.application.services.VerificationService;
import com.assembliestore.api.module.user.domain.repository.UserRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthPort _authPort;

    public AuthController(
            UserRepository userRepository,
            VerificationService verificationService,
            PasswordEncoder passwordEncoder,
            AuthPort authPort) {

        this._authPort = authPort;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody final SignInRequest request) {

        JwtTokenDto jwtTokenDto = _authPort.authentication(SignInMapper.toSignAuthenticationCommand(request));

        TokenResponse tokenResponse = SignUpMapper.toTokenResponse(jwtTokenDto);

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {

        JwtTokenDto jwtTokenDto = _authPort.register(SignUpMapper.toRegisterCommand(request));

        TokenResponse tokenResponse = SignUpMapper.toTokenResponse(jwtTokenDto);

        return new ResponseEntity<>(tokenResponse, HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
        @RequestHeader("Authorization") String authorizationHeader
        ) {

        //String authorizationHeader = "Bearer eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJjOTFmMGIxZS0zNmY1LTRmOWItYTE0YS0yZTVmZGUzZjNlZjUiLCJ1c2VyTmFtZSI6ImFsaWNlMDEiLCJzdWIiOiJhbGljZTAxQGV4YW1wbGUuY29tIiwiaWF0IjoxNzUyOTc0MDQ4LCJleHAiOjE3NTI5NzQ2NTN9.w9rGYV0aTjbl-1Jb6WXiE065jM9PKYOL1DsSKePUj7RUaFmq5O3ui1aSaoKQbdUHvM_0QeIXy34cHs4ejMmsmg";
        System.out.println("Refresh endpoint called. Token: " + authorizationHeader);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new CredentialInvalidExeption("El token de actualización es inválido");
        }

        JwtTokenDto jwtTokenDto = _authPort.refreshToken(authorizationHeader);

        TokenResponse tokenResponse = SignUpMapper.toTokenResponse(jwtTokenDto);

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        final String jwtToken  = authHeader.substring(7);

        _authPort.logout(jwtToken);

        return ResponseEntity.ok().build();
    }

}
