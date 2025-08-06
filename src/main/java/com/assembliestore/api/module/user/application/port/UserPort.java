package com.assembliestore.api.module.user.application.port;

import com.assembliestore.api.module.user.application.dto.UserDto;

public interface UserPort {

    UserDto findByEmail(String userName);
} 
