package com.assembliestore.api.service.storage.dto;

public class FileUploadResponse {
    
    private boolean success;
    private String message;
    private String publicId;
    private String secureUrl;
    private String url;
    private String format;
    private String resourceType;
    private long bytes;
    private int width;
    private int height;
    private String originalFileName;
    private String folder;

    public FileUploadResponse() {}

    public FileUploadResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static FileUploadResponse success(String publicId, String secureUrl, String url,
                                           String format, String resourceType, long bytes,
                                           int width, int height, String originalFileName,
                                           String folder) {
        FileUploadResponse response = new FileUploadResponse();
        response.success = true;
        response.message = "Archivo subido exitosamente";
        response.publicId = publicId;
        response.secureUrl = secureUrl;
        response.url = url;
        response.format = format;
        response.resourceType = resourceType;
        response.bytes = bytes;
        response.width = width;
        response.height = height;
        response.originalFileName = originalFileName;
        response.folder = folder;
        return response;
    }

    public static FileUploadResponse error(String message) {
        return new FileUploadResponse(false, message);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
