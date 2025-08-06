package com.assembliestore.api.common.interfaces;

public class SuccessfulResponse {

    public SuccessfulResponse(String message) {
        this.success = "true";
        this.message = message;
    }

    private String success;

    private String message;

    public String getSuccess() {

        return success;
    }

    public String getMessage() {

        return message;
    }

}
