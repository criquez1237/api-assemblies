package com.assembliestore.api.module.user.application.dto;

import java.util.Date;

import com.assembliestore.api.module.user.common.enums.TokenType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtTokenDto
{
        private String id;
        private String token;
        private String refreshToken;
        private TokenType type;
        private boolean revoked;
        private boolean expired;
        private String userId;
        private Date updatedAt;
}
