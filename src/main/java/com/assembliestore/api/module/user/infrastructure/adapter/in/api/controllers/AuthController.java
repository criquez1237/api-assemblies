package com.assembliestore.api.module.user.infrastructure.adapter.in.api.controllers;

import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignInRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignUpRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignUpResponse;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.VerifyOTPRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.VerifyOTPResponse;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Endpoints de autenticación")
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
    @Operation(summary = "Iniciar sesión", description = "Permite a un usuario iniciar sesión en la aplicación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<?> signin(@RequestBody final SignInRequest request) {

        JwtTokenDto jwtTokenDto = _authPort.authentication(SignInMapper.toSignAuthenticationCommand(request));

        TokenResponse tokenResponse = SignUpMapper.toTokenResponse(jwtTokenDto);

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/signup")
    @Operation(summary = "Registrarse", description = "Registra un nuevo usuario y envía código OTP para activación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registro exitoso - Se envió código OTP"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
        try {
            // El método registerWithOTP no devuelve tokens, solo registra y envía OTP
            _authPort.registerWithOTP(SignUpMapper.toRegisterCommand(request));

            SignUpResponse response = SignUpResponse.success(request.getEmail());
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            SignUpResponse response = SignUpResponse.error("Error en el registro: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verificar OTP", description = "Verifica el código OTP y activa la cuenta del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verificado - Cuenta activada"),
            @ApiResponse(responseCode = "400", description = "Código OTP inválido o expirado")
    })
    public ResponseEntity<?> verifyOTP(@RequestBody VerifyOTPRequest request) {
        try {
            JwtTokenDto jwtTokenDto = _authPort.verifyOTPAndActivate(request.getEmail(), request.getOtpCode());
            
            TokenResponse tokenResponse = SignUpMapper.toTokenResponse(jwtTokenDto);
            VerifyOTPResponse response = VerifyOTPResponse.success(tokenResponse);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            VerifyOTPResponse response = VerifyOTPResponse.error("Error al verificar OTP: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Actualizar token", description = "Permite a un usuario actualizar su token de acceso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token actualizado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token de actualización inválido")
    })
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
    @Operation(summary = "Cerrar sesión", description = "Permite a un usuario cerrar sesión en la aplicación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cierre de sesión exitoso"),
            @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<?> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        final String jwtToken  = authHeader.substring(7);

        _authPort.logout(jwtToken);

        return ResponseEntity.ok().build();
    }

}
