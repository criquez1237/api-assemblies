package com.assembliestore.api.service.storage.controller;

import com.assembliestore.api.common.response.ApiResponse;
import com.assembliestore.api.service.storage.service.CloudinaryService;
import com.assembliestore.api.service.storage.dto.FileUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@Tag(name = "File Upload", description = "Endpoints para subida segura de archivos")
@CrossOrigin(originPatterns = "*")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir archivo", description = "Sube un archivo de forma segura con validación de tipos")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        
        try {
            logger.info("Iniciando subida de archivo: {} (tamaño: {} bytes)", 
                file.getOriginalFilename(), file.getSize());

            FileUploadResponse response = cloudinaryService.uploadFile(file, folder);
            
            logger.info("Archivo subido exitosamente: {} -> {}", 
                file.getOriginalFilename(), response.getPublicId());

            return ResponseEntity.ok(ApiResponse.success(
                "Archivo subido exitosamente",
                response
            ));

        } catch (IllegalArgumentException e) {
            logger.warn("Archivo rechazado por validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Archivo no válido: " + e.getMessage()
            ));
            
        } catch (Exception e) {
            logger.error("Error al subir archivo {}: {}", file.getOriginalFilename(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                    "Error interno al procesar el archivo"
                ));
        }
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen", description = "Sube una imagen con validación específica")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        
        try {
            logger.info("Iniciando subida de imagen: {} (tamaño: {} bytes)", 
                file.getOriginalFilename(), file.getSize());

            FileUploadResponse response = cloudinaryService.uploadImage(file, folder, true);
            
            logger.info("Imagen subida exitosamente: {} -> {}", 
                file.getOriginalFilename(), response.getPublicId());

            return ResponseEntity.ok(ApiResponse.success(
                "Imagen subida exitosamente",
                response
            ));

        } catch (IllegalArgumentException e) {
            logger.warn("Imagen rechazada por validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Imagen no válida: " + e.getMessage()
            ));
            
        } catch (Exception e) {
            logger.error("Error al subir imagen {}: {}", file.getOriginalFilename(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                    "Error interno al procesar la imagen"
                ));
        }
    }

    @DeleteMapping("/{publicId}")
    @Operation(summary = "Eliminar archivo", description = "Elimina un archivo de Cloudinary")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String publicId) {
        try {
            logger.info("Eliminando archivo: {}", publicId);
            
            boolean deleted = cloudinaryService.deleteFile(publicId);
            
            if (deleted) {
                logger.info("Archivo eliminado exitosamente: {}", publicId);
                return ResponseEntity.ok(ApiResponse.success("Archivo eliminado exitosamente", null));
            } else {
                logger.warn("No se pudo eliminar el archivo: {}", publicId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Archivo no encontrado"));
            }
            
        } catch (Exception e) {
            logger.error("Error al eliminar archivo {}: {}", publicId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                    "Error interno al eliminar el archivo"
                ));
        }
    }

    @GetMapping("/transform/{publicId}")
    @Operation(summary = "Obtener URL transformada", description = "Genera URL con transformaciones para una imagen")
    public ResponseEntity<ApiResponse<String>> getTransformationUrl(
            @PathVariable String publicId,
            @RequestParam(value = "width", required = false, defaultValue = "300") int width,
            @RequestParam(value = "height", required = false, defaultValue = "300") int height,
            @RequestParam(value = "quality", required = false, defaultValue = "auto") String quality) {
        
        try {
            logger.debug("Generando URL de transformación para: {} ({}x{}, quality: {})", 
                publicId, width, height, quality);

            String url = cloudinaryService.generateTransformationUrl(publicId, width, height, quality);
            
            if (url != null) {
                return ResponseEntity.ok(ApiResponse.success(
                    "URL generada exitosamente",
                    url
                ));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error(
                    "No se pudo generar la URL de transformación"
                ));
            }
            
        } catch (Exception e) {
            logger.error("Error al generar URL de transformación para {}: {}", publicId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                    "Error interno al generar la URL"
                ));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Verificar conexión", description = "Verifica la conectividad con Cloudinary")
    public ResponseEntity<ApiResponse<String>> checkConnection() {
        try {
            // Verificamos la configuración de Cloudinary
            return ResponseEntity.ok(ApiResponse.success(
                "Servicio de archivos disponible",
                "OK"
            ));
            
        } catch (Exception e) {
            logger.error("Error al verificar el servicio: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(
                    "Error al verificar el servicio"
                ));
        }
    }
}
