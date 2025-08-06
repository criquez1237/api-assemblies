package com.assembliestore.api.module.user.application.mapper;

import com.assembliestore.api.module.user.application.dto.UserDto;
import com.assembliestore.api.module.user.domain.entities.User;

public class UserMapper {


    public static UserDto toUserDto(User user) {

        return new UserDto(
            user.getId(),
            user.getUserName(),
            user.getEmail(),
            user.getPassword(),
            user.getPerfil(),
            user.getRole(),
            user.isVerified(),
            user.isActived(),
            user.isDeleted(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getDeletedAt()
        );
    }

}
