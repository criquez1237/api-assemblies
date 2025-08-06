package com.assembliestore.api.module.user.application.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.assembliestore.api.module.user.application.dto.JwtTokenDto;
import com.assembliestore.api.module.user.application.dto.UserDto;
import com.assembliestore.api.module.user.application.port.TokenPort;
import com.assembliestore.api.module.user.common.enums.TokenType;
import com.assembliestore.api.module.user.domain.entities.Perfil;
import com.assembliestore.api.module.user.domain.entities.Token;
import com.assembliestore.api.module.user.domain.entities.User;
import com.assembliestore.api.module.user.domain.port.JwtPort;
import com.assembliestore.api.module.user.domain.repository.TokenRepository;

@Service
public class JwtTokenService implements TokenPort {

    private final JwtPort _jwtPort;
    private final TokenRepository _tokenRepository;

    public JwtTokenService(TokenRepository tokenRepository, JwtPort jwtPort) {
        this._jwtPort = jwtPort;
        this._tokenRepository = tokenRepository;
    }

    public JwtTokenDto generateToken(User user) {

        String jwtToken = _jwtPort.generatedToken(user);
        String jwtRefreshToken = _jwtPort.generatedRefreshToken(user);

        Token token = Token.builder()
                .id(user.getId())
                .token(jwtToken)
                .refreshToken(jwtRefreshToken)
                .userId(user.getId())
                .revoked(false)
                .expired(false)
                .type(TokenType.BEARER)
                .updatedAt(new java.sql.Date(Instant.now().toEpochMilli()))
                .build();

        _tokenRepository.saveToken(token);

        return JwtTokenDto.builder()
                .id(user.getId())
                .token(jwtToken)
                .refreshToken(jwtRefreshToken)
                .type(TokenType.BEARER)
                .revoked(token.isRevoked())
                .expired(token.isExpired())
                .userId(user.getId())
                .updatedAt(token.getUpdatedAt())
                .build();
    }

    @Override
    public String getUserNameFromToken(String token) {

        String userName = _jwtPort.getUserNameFromToken(token);
        return userName;
    }

    @Override
    public JwtTokenDto findByToken(String token) {

        Token tokenResponse = _tokenRepository.findByToken(token).get();

        JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
                .id(tokenResponse.getId())
                .token(tokenResponse.getToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .type(tokenResponse.getType())
                .revoked(tokenResponse.isRevoked())
                .expired(tokenResponse.isExpired())
                .userId(tokenResponse.getUserId())
                .updatedAt(tokenResponse.getUpdatedAt())
                .build();

        return jwtTokenDto;
    }

    @Override
    public boolean validateToken(String token, UserDto userDto) {

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUserName("ekjricnriu");
        user.setEmail(userDto.email());
        user.setPassword(userDto.password());
        user.setRole(com.assembliestore.api.module.user.common.enums.Role.CLIENT); // Por defecto cliente
        Perfil perfil = new Perfil();
        perfil.setNames(userDto.perfil().getNames());
        perfil.setSurnames(userDto.perfil().getSurnames());
        perfil.setImagePerfil(userDto.perfil().getImagePerfil());
        perfil.setPhone(userDto.perfil().getPhone());
        user.setPerfil(perfil);

        return this._jwtPort.validateToken(token, user);
    }

    @Override
    public void updateToken(JwtTokenDto jwtTokenDto) {
        
        Token token = Token.builder()
                .id(jwtTokenDto.getId())
                .token(jwtTokenDto.getToken())
                .refreshToken(jwtTokenDto.getRefreshToken())
                .type(jwtTokenDto.getType())
                .revoked(jwtTokenDto.isRevoked())
                .expired(jwtTokenDto.isExpired())
                .userId(jwtTokenDto.getUserId())
                .updatedAt(jwtTokenDto.getUpdatedAt())
                .build();

        _tokenRepository.saveToken(token);
    }
}
// .id(UUID.randomUUID().toString())