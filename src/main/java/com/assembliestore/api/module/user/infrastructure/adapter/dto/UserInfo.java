package com.assembliestore.api.module.user.infrastructure.adapter.dto;

import com.assembliestore.api.module.user.common.enums.Role;

public record UserInfo(
    String email,
    Role role,
    PerfilDto perfil
) {}
