package com.assembliestore.api.module.user.application.command;

public record AuthenticationCommand(
        String email,
        String password) {
}
