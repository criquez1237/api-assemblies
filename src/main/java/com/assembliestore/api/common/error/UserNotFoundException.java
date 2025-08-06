package com.assembliestore.api.common.error;

public class UserNotFoundException extends InfrastructureException {

    public UserNotFoundException(String message) {

        super(message);
    }
}
