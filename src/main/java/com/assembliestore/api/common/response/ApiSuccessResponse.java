package com.assembliestore.api.common.response;

public class ApiSuccessResponse<T> {
    private String status = "success";
    private String message;
    private String code;
    private T data;
    private TechnicalDetails technicalDetails;

    public ApiSuccessResponse() {}

    public ApiSuccessResponse(String message, String code, T data, TechnicalDetails tech) {
        this.message = message;
        this.code = code;
        this.data = data;
        this.technicalDetails = tech;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public TechnicalDetails getTechnicalDetails() { return technicalDetails; }
    public void setTechnicalDetails(TechnicalDetails technicalDetails) { this.technicalDetails = technicalDetails; }
}
