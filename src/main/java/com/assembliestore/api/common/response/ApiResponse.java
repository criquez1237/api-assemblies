package com.assembliestore.api.common.response;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String success;
    private String message;
    private T data;

    private ApiResponse() {}

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = "true";
        response.message = message;
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = "false";
        response.message = message;
        response.data = null;
        return response;
    }
}
