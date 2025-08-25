package com.assembliestore.api.module.user.infrastructure.adapter.dto;

public class TokenWithUserResponse {
    private TokenResponse tokens;
    private UserInfo user;
    private AdditionalDataDto additionalData;

    public TokenWithUserResponse() {}

    public TokenWithUserResponse(TokenResponse tokens, UserInfo user) {
        this.tokens = tokens;
        this.user = user;
    }

    public TokenResponse getTokens() { return tokens; }
    public void setTokens(TokenResponse tokens) { this.tokens = tokens; }
    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }
    public AdditionalDataDto getAdditionalData() { return additionalData; }
    public void setAdditionalData(AdditionalDataDto additionalData) { this.additionalData = additionalData; }
}
