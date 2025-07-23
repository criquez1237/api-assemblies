package com.assembliestore.api.service.storage.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum AllowedFileType {
    // Imágenes
    JPEG("image/jpeg", new String[]{"jpg", "jpeg"}, 10 * 1024 * 1024), // 10MB
    PNG("image/png", new String[]{"png"}, 10 * 1024 * 1024),
    WEBP("image/webp", new String[]{"webp"}, 10 * 1024 * 1024),
    GIF("image/gif", new String[]{"gif"}, 5 * 1024 * 1024), // 5MB
    
    // Videos (formatos más comunes)
    MP4("video/mp4", new String[]{"mp4"}, 100 * 1024 * 1024), // 100MB
    WEBM("video/webm", new String[]{"webm"}, 100 * 1024 * 1024),
    MOV("video/quicktime", new String[]{"mov"}, 100 * 1024 * 1024),
    AVI("video/x-msvideo", new String[]{"avi"}, 100 * 1024 * 1024),
    
    // Documentos
    PDF("application/pdf", new String[]{"pdf"}, 50 * 1024 * 1024), // 50MB
    
    // Microsoft Office
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
         new String[]{"docx"}, 25 * 1024 * 1024), // 25MB
    DOC("application/msword", new String[]{"doc"}, 25 * 1024 * 1024),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
         new String[]{"xlsx"}, 25 * 1024 * 1024),
    XLS("application/vnd.ms-excel", new String[]{"xls"}, 25 * 1024 * 1024),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", 
         new String[]{"pptx"}, 25 * 1024 * 1024),
    PPT("application/vnd.ms-powerpoint", new String[]{"ppt"}, 25 * 1024 * 1024);

    private final String mimeType;
    private final String[] extensions;
    private final long maxSize;

    AllowedFileType(String mimeType, String[] extensions, long maxSize) {
        this.mimeType = mimeType;
        this.extensions = extensions;
        this.maxSize = maxSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public boolean hasExtension(String extension) {
        return Arrays.stream(extensions)
                .anyMatch(ext -> ext.equalsIgnoreCase(extension));
    }

    public static AllowedFileType fromMimeType(String mimeType) {
        return Arrays.stream(values())
                .filter(type -> type.mimeType.equals(mimeType))
                .findFirst()
                .orElse(null);
    }

    public static AllowedFileType fromExtension(String extension) {
        return Arrays.stream(values())
                .filter(type -> type.hasExtension(extension))
                .findFirst()
                .orElse(null);
    }

    public static Set<String> getAllAllowedExtensions() {
        return Arrays.stream(values())
                .flatMap(type -> Arrays.stream(type.extensions))
                .collect(Collectors.toSet());
    }

    public static Set<String> getAllAllowedMimeTypes() {
        return Arrays.stream(values())
                .map(AllowedFileType::getMimeType)
                .collect(Collectors.toSet());
    }

    public boolean isImage() {
        return this.mimeType.startsWith("image/");
    }

    public boolean isVideo() {
        return this.mimeType.startsWith("video/");
    }

    public boolean isDocument() {
        return this.mimeType.startsWith("application/");
    }
}
