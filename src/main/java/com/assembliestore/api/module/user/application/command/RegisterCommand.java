package com.assembliestore.api.module.user.application.command;

public record RegisterCommand(

        //String userName,
        String email,
        String password,
        String names,
        String surnames,
        String imagePerfil,
        String phone) {

}
