package com.assembliestore.api.common.response;

import java.util.List;
import java.time.Instant;

public class ApiErrorResponse {
    private String status = "fail";
    private String message;
    private String code;
    private List<ErrorDetail> details;
    private TechnicalDetails technicalDetails;

    public ApiErrorResponse() {}

    public ApiErrorResponse(String message, String code, List<ErrorDetail> details, TechnicalDetails tech) {
        this.message = message;
        this.code = code;
        this.details = details;
        this.technicalDetails = tech;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public List<ErrorDetail> getDetails() { return details; }
    public void setDetails(List<ErrorDetail> details) { this.details = details; }
    public TechnicalDetails getTechnicalDetails() { return technicalDetails; }
    public void setTechnicalDetails(TechnicalDetails technicalDetails) { this.technicalDetails = technicalDetails; }
}
