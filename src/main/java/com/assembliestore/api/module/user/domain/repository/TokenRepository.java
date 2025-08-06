package com.assembliestore.api.module.user.domain.repository;

import java.util.Optional;

import com.assembliestore.api.module.user.domain.entities.Token;

public interface TokenRepository {

    void saveToken(Token token);

    Optional<Token> findByToken(String token);
}
