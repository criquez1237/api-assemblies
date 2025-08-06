package com.assembliestore.api.common.error;

public class InfrastructureException extends RuntimeException {

    public InfrastructureException(String message) {
        super(message);
    }
        public InfrastructureException(Throwable cause) {
        super(cause);
    }

    public InfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
