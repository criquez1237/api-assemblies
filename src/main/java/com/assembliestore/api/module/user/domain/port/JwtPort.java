package com.assembliestore.api.module.user.domain.port;

import com.assembliestore.api.module.user.domain.entities.User;

public interface JwtPort {

    String generatedToken(final User user);

    String generatedRefreshToken(final User user);

    boolean validateToken(String token, User user);

    String getUserNameFromToken(final String token);
}
