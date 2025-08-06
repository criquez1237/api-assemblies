package com.assembliestore.api.common.error;

public class EmailAlreadyExistsException extends InfrastructureException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

}
