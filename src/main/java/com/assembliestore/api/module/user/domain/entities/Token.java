package com.assembliestore.api.module.user.domain.entities;

import java.util.Date;

import com.assembliestore.api.module.user.common.enums.TokenType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    private String id;

    private String token;

    private String refreshToken;

    private TokenType type;

    private boolean revoked;

    private boolean expired;

    private String userId;

    private Date updatedAt;

}
