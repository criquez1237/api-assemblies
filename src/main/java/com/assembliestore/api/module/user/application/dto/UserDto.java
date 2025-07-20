package com.assembliestore.api.module.user.application.dto;

import java.util.Date;

import com.assembliestore.api.module.user.common.enums.Role;
import com.assembliestore.api.module.user.domain.entities.Perfil;

public record UserDto(

        String id,
        String userName,
        String email,
        String password,
        Perfil perfil,
        Role role,
        boolean verified,
        boolean actived,
        boolean deleted,
        Date createdAt,
        Date updatedAt,
        Date deletedAt
) {

}
