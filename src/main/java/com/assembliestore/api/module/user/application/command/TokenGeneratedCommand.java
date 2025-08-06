package com.assembliestore.api.module.user.application.command;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenGeneratedCommand(
        @JsonProperty("access_token") String accessToken,

        @JsonProperty("refresh_token") String refreshToken) {

}
