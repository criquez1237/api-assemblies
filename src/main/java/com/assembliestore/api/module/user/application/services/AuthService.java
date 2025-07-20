package com.assembliestore.api.module.user.application.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.assembliestore.api.module.user.application.command.RegisterCommand;
import com.assembliestore.api.common.error.CredentialInvalidExeption;
import com.assembliestore.api.common.error.EmailAlreadyExistsException;
import com.assembliestore.api.common.error.InfrastructureException;
import com.assembliestore.api.module.user.application.command.AuthenticationCommand;
import com.assembliestore.api.module.user.application.dto.JwtTokenDto;
import com.assembliestore.api.module.user.application.port.AuthPort;
import com.assembliestore.api.module.user.domain.entities.Perfil;
import com.assembliestore.api.module.user.domain.entities.Token;
import com.assembliestore.api.module.user.domain.entities.User;
import com.assembliestore.api.module.user.domain.port.JwtPort;
import com.assembliestore.api.module.user.domain.repository.TokenRepository;
import com.assembliestore.api.module.user.domain.repository.UserRepository;

@Service
public class AuthService implements AuthPort {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository _userRepository;
    private final JwtTokenService _tokenService;
    private final AuthenticationManager _authenticationManager;
    private final TokenRepository _tokenRepository;
    private final JwtPort _jwtPort;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService tokenService,
            AuthenticationManager authenticationManager,
            JwtPort jwtPort,
            TokenRepository tokenRepository
            ) {

        this._userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this._tokenService = tokenService;
        this._authenticationManager = authenticationManager;
        this._jwtPort = jwtPort;
        this._tokenRepository = tokenRepository;
    }

    @Override
    public JwtTokenDto register(RegisterCommand command) {

        if (this._userRepository.findByEmail(command.email()).isPresent()) {
            throw new EmailAlreadyExistsException("El correo ya está registrado");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUserName("ekjricnriu");
        user.setEmail(command.email());
        user.setPassword(passwordEncoder.encode(command.password()));
        user.setRole(com.assembliestore.api.module.user.common.enums.Role.CLIENT); // Por defecto cliente
        Perfil perfil = new Perfil();
        perfil.setNames(command.names());
        perfil.setSurnames(command.surnames());
        perfil.setImagePerfil(command.imagePerfil());
        perfil.setPhone(command.phone());
        user.setPerfil(perfil);

        this._userRepository.create(user);

        JwtTokenDto tokens = _tokenService.generateToken(user);

        return tokens;
    }

    @Override
    public JwtTokenDto authentication(AuthenticationCommand command) {

        try {

            _authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            command.email(),
                            command.password()));

            Optional<User> response = _userRepository.findByEmail(command.email());

            /*
             * if (!response.isPresent()) {
             * throw new CredentialInvalidExeption(
             * "El usuario no existe o las credenciales son invalidas");
             * }
             */

            // mejorar codigo mas despues
            User user = response.get();
            /*
             * if (!passwordEncoder.matches(command.password(), user.getPassword())) {
             * throw new CredentialInvalidExeption(
             * "El usuario no existe o las credenciales son invalidas");
             * }
             */
            if (!user.isActived()) {
                throw new CredentialInvalidExeption(
                        "El usuario no existe o las credenciales son invalidas");
            }

            JwtTokenDto tokens = _tokenService.generateToken(user);

            return tokens;

        } catch (CredentialInvalidExeption error) {
            throw error;

        } catch (Exception error) {
            throw new InfrastructureException("A ocurrido un error interno");
        }
    }

    @Override
    public JwtTokenDto refreshToken(String refreshToken) {

        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {

            throw new CredentialInvalidExeption("El token de actualización es inválido");
        }

        final String token = refreshToken.substring(7);

        final String userName = _jwtPort.getUserNameFromToken(token);

        if (userName == null) {

            throw new CredentialInvalidExeption("El token de actualización es inválido");
        }

        Optional<User> user = _userRepository.findByEmail(userName);

        if (!user.isPresent()) {
            throw new CredentialInvalidExeption(
                    "El usuario no existe o las credenciales son invalidas");
        }

        if (!_jwtPort.validateToken(token, user.get())) {

            throw new CredentialInvalidExeption("El token de actualización es inválido");
        }

        final JwtTokenDto accessToken = _tokenService.generateToken(user.get());

        return accessToken;
    }

    @Override
    public JwtTokenDto logout(String token) {

        final Optional<Token> jwtTokenDto = _tokenRepository.findByToken(token);

        if (!jwtTokenDto.isPresent()) {
            throw new CredentialInvalidExeption("El token no existe");
        }

        jwtTokenDto.get().setExpired(true);
        jwtTokenDto.get().setRevoked(true);


        JwtTokenDto updatedToken = JwtTokenDto.builder()
                .id(jwtTokenDto.get().getId())
                .token(jwtTokenDto.get().getToken())
                .refreshToken(jwtTokenDto.get().getRefreshToken())
                .type(jwtTokenDto.get().getType())
                .revoked(jwtTokenDto.get().isRevoked())
                .expired(jwtTokenDto.get().isExpired())
                .userId(jwtTokenDto.get().getUserId())
                .updatedAt(jwtTokenDto.get().getUpdatedAt())
                .build();

        _tokenService.updateToken(updatedToken);

        return updatedToken;
    }
    
}
