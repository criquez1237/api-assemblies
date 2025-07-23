package com.assembliestore.api.service.storage.dto;

import com.assembliestore.api.service.storage.enums.AllowedFileType;

public class FileValidationResult {
    
    private boolean valid;
    private String errorMessage;
    private AllowedFileType detectedType;
    private String detectedMimeType;
    private String originalFileName;
    private long fileSize;
    private String sanitizedFileName;

    public FileValidationResult() {}

    public FileValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    public static FileValidationResult success(AllowedFileType type, String detectedMimeType, 
                                             String originalFileName, long fileSize, String sanitizedFileName) {
        FileValidationResult result = new FileValidationResult();
        result.valid = true;
        result.detectedType = type;
        result.detectedMimeType = detectedMimeType;
        result.originalFileName = originalFileName;
        result.fileSize = fileSize;
        result.sanitizedFileName = sanitizedFileName;
        return result;
    }

    public static FileValidationResult error(String errorMessage) {
        return new FileValidationResult(false, errorMessage);
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public AllowedFileType getDetectedType() {
        return detectedType;
    }

    public void setDetectedType(AllowedFileType detectedType) {
        this.detectedType = detectedType;
    }

    public String getDetectedMimeType() {
        return detectedMimeType;
    }

    public void setDetectedMimeType(String detectedMimeType) {
        this.detectedMimeType = detectedMimeType;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSanitizedFileName() {
        return sanitizedFileName;
    }

    public void setSanitizedFileName(String sanitizedFileName) {
        this.sanitizedFileName = sanitizedFileName;
    }
}
