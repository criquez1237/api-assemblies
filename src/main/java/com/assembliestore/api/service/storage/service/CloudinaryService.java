package com.assembliestore.api.service.storage.service;

import com.assembliestore.api.service.storage.config.CloudinaryConfig;
import com.assembliestore.api.service.storage.dto.FileUploadResponse;
import com.assembliestore.api.service.storage.dto.FileValidationResult;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryService.class);

    private final Cloudinary cloudinary;
    private final FileValidationService fileValidationService;

    public CloudinaryService(CloudinaryConfig config, FileValidationService fileValidationService) {
        this.fileValidationService = fileValidationService;
        
        // Inicializar Cloudinary con configuración segura
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", config.getCloudName(),
            "api_key", config.getApiKey(),
            "api_secret", config.getApiSecret(),
            "secure", config.isSecure()
        ));
        
        logger.info("Cloudinary service initialized with cloud_name: {}", config.getCloudName());
    }

    /**
     * Subir archivo con validación completa de seguridad
     */
    public FileUploadResponse uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, null);
    }

    /**
     * Subir archivo con carpeta personalizada y public_id
     */
    public FileUploadResponse uploadFile(MultipartFile file, String folder, String customPublicId) {
        try {
            // 1. Validación completa del archivo
            FileValidationResult validation = fileValidationService.validateFile(file);
            if (!validation.isValid()) {
                logger.warn("File validation failed: {}", validation.getErrorMessage());
                return FileUploadResponse.error("Validación fallida: " + validation.getErrorMessage());
            }

            // 2. Configurar parámetros de subida seguros
            String publicId = customPublicId != null ? customPublicId : generateSecurePublicId();
            String sanitizedFolder = sanitizeFolder(folder);
            
            Map<String, Object> uploadParams = buildUploadParams(validation, publicId, sanitizedFolder);
            
            // 3. Subir archivo a Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            
            // 4. Crear respuesta exitosa
            FileUploadResponse response = mapToResponse(uploadResult, validation.getOriginalFileName(), sanitizedFolder);
            
            logger.info("File uploaded successfully: {} -> {}", 
                       validation.getOriginalFileName(), response.getPublicId());
            
            return response;

        } catch (IOException e) {
            logger.error("Failed to upload file: {}", e.getMessage(), e);
            return FileUploadResponse.error("Error al subir archivo: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during file upload: {}", e.getMessage(), e);
            return FileUploadResponse.error("Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Subir imagen con transformaciones automáticas
     */
    public FileUploadResponse uploadImage(MultipartFile file, String folder, boolean optimize) {
        // Validar que sea imagen
        if (!fileValidationService.isValidImage(file)) {
            return FileUploadResponse.error("El archivo no es una imagen válida");
        }

        try {
            FileValidationResult validation = fileValidationService.validateFile(file);
            String publicId = generateSecurePublicId();
            String sanitizedFolder = sanitizeFolder(folder);
            
            Map<String, Object> uploadParams = buildImageUploadParams(validation, publicId, sanitizedFolder, optimize);
            
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            
            FileUploadResponse response = mapToResponse(uploadResult, validation.getOriginalFileName(), sanitizedFolder);
            
            logger.info("Image uploaded with optimizations: {} -> {}", 
                       validation.getOriginalFileName(), response.getPublicId());
            
            return response;

        } catch (Exception e) {
            logger.error("Failed to upload image: {}", e.getMessage(), e);
            return FileUploadResponse.error("Error al subir imagen: " + e.getMessage());
        }
    }

    /**
     * Eliminar archivo por public_id
     */
    public boolean deleteFile(String publicId) {
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");
            
            boolean success = "ok".equals(resultStatus);
            
            if (success) {
                logger.info("File deleted successfully: {}", publicId);
            } else {
                logger.warn("Failed to delete file: {} - Status: {}", publicId, resultStatus);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error deleting file {}: {}", publicId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generar URL de transformación segura para imágenes
     */
    @SuppressWarnings("rawtypes")
    public String generateTransformationUrl(String publicId, int width, int height, String quality) {
        try {
            Transformation transformation = new Transformation()
                .width(width)
                .height(height)
                .crop("fill")
                .quality(quality != null ? quality : "auto")
                .fetchFormat("auto");
                
            return cloudinary.url()
                .transformation(transformation)
                .secure(true)
                .generate(publicId);
        } catch (Exception e) {
            logger.error("Error generating transformation URL for {}: {}", publicId, e.getMessage());
            return null;
        }
    }

    /**
     * Construir parámetros de subida seguros
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> buildUploadParams(FileValidationResult validation, String publicId, String folder) {
        return ObjectUtils.asMap(
            "public_id", publicId,
            "folder", folder,
            "resource_type", determineResourceType(validation.getDetectedType()),
            "secure", true,
            "use_filename", false, // No usar nombre original por seguridad
            "unique_filename", true,
            "overwrite", false, // No sobrescribir archivos existentes
            "format", validation.getDetectedType().getExtensions()[0],
            // Metadatos para auditoría
            "context", ObjectUtils.asMap(
                "original_name", validation.getOriginalFileName(),
                "upload_time", System.currentTimeMillis(),
                "file_size", validation.getFileSize()
            )
        );
    }

    /**
     * Construir parámetros específicos para imágenes con optimización
     */
    private Map<String, Object> buildImageUploadParams(FileValidationResult validation, String publicId, 
                                                      String folder, boolean optimize) {
        Map<String, Object> params = buildUploadParams(validation, publicId, folder);
        
        if (optimize) {
            params.put("quality", "auto");
            params.put("fetch_format", "auto");
            params.put("flags", "progressive");
        }
        
        return params;
    }

    /**
     * Mapear resultado de Cloudinary a nuestro DTO
     */
    private FileUploadResponse mapToResponse(Map<?, ?> uploadResult, String originalFileName, String folder) {
        return FileUploadResponse.success(
            (String) uploadResult.get("public_id"),
            (String) uploadResult.get("secure_url"),
            (String) uploadResult.get("url"),
            (String) uploadResult.get("format"),
            (String) uploadResult.get("resource_type"),
            ((Number) uploadResult.get("bytes")).longValue(),
            uploadResult.get("width") != null ? ((Number) uploadResult.get("width")).intValue() : 0,
            uploadResult.get("height") != null ? ((Number) uploadResult.get("height")).intValue() : 0,
            originalFileName,
            folder
        );
    }

    /**
     * Determinar tipo de recurso para Cloudinary
     */
    private String determineResourceType(com.assembliestore.api.service.storage.enums.AllowedFileType type) {
        if (type.isImage()) {
            return "image";
        } else if (type.isVideo()) {
            return "video";
        } else {
            return "raw"; // Para documentos
        }
    }

    /**
     * Generar public_id seguro y único
     */
    private String generateSecurePublicId() {
        return "file_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Sanitizar nombre de carpeta
     */
    private String sanitizeFolder(String folder) {
        if (folder == null || folder.trim().isEmpty()) {
            return "uploads";
        }
        
        // Remover caracteres peligrosos y limitaciones de Cloudinary
        return folder.replaceAll("[^a-zA-Z0-9/_-]", "_")
                    .replaceAll("_{2,}", "_")
                    .toLowerCase()
                    .substring(0, Math.min(folder.length(), 50));
    }

    /**
     * Verificar configuración de Cloudinary
     */
    public boolean isConfigured() {
        try {
            Map<?, ?> result = cloudinary.api().ping(ObjectUtils.emptyMap());
            return result != null && "ok".equals(result.get("status"));
        } catch (Exception e) {
            logger.error("Cloudinary configuration check failed: {}", e.getMessage());
            return false;
        }
    }
}
