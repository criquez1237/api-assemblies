package com.assembliestore.api.service.storage.service;

import com.assembliestore.api.service.storage.dto.FileValidationResult;
import com.assembliestore.api.service.storage.enums.AllowedFileType;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

@Service
public class FileValidationService {

    private final Tika tika = new Tika();
    
    // Patrones peligrosos en nombres de archivo
    private static final Pattern DANGEROUS_FILE_PATTERN = Pattern.compile(
        ".*\\.(exe|bat|cmd|com|pif|scr|vbs|js|jar|sh|ps1|psm1|reg|msi|dll|bin|iso|img)$",
        Pattern.CASE_INSENSITIVE
    );
    
    // Patrones para extensiones dobles sospechosas
    private static final Pattern DOUBLE_EXTENSION_PATTERN = Pattern.compile(
        ".*\\.(exe|bat|cmd|com|pif|scr|vbs|js|jar|sh|ps1)\\.(jpg|jpeg|png|gif|pdf|doc|docx|xls|xlsx|txt)$",
        Pattern.CASE_INSENSITIVE
    );

    // Caracteres peligrosos en nombres de archivo
    private static final Pattern DANGEROUS_CHARS_PATTERN = Pattern.compile(
        "[<>:\"/\\\\|?*\\x00-\\x1F]"
    );

    /**
     * Validación completa de archivo con múltiples capas de seguridad
     */
    public FileValidationResult validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return FileValidationResult.error("El archivo está vacío");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.trim().isEmpty()) {
            return FileValidationResult.error("Nombre de archivo inválido");
        }

        // 1. Validar nombre de archivo por patrones peligrosos
        FileValidationResult nameValidation = validateFileName(originalFileName);
        if (!nameValidation.isValid()) {
            return nameValidation;
        }

        // 2. Validar extensión doble sospechosa
        FileValidationResult doubleExtValidation = validateDoubleExtension(originalFileName);
        if (!doubleExtValidation.isValid()) {
            return doubleExtValidation;
        }

        // 3. Detectar MIME type real del archivo usando Apache Tika
        String detectedMimeType;
        try (InputStream inputStream = file.getInputStream()) {
            detectedMimeType = tika.detect(inputStream, originalFileName);
        } catch (IOException e) {
            return FileValidationResult.error("Error al leer el archivo: " + e.getMessage());
        }

        // 4. Validar que el MIME type sea permitido
        AllowedFileType allowedType = AllowedFileType.fromMimeType(detectedMimeType);
        if (allowedType == null) {
            return FileValidationResult.error(
                "Tipo de archivo no permitido. MIME detectado: " + detectedMimeType
            );
        }

        // 5. Verificar consistencia entre extensión del nombre y MIME type real
        String fileExtension = getFileExtension(originalFileName);
        if (!allowedType.hasExtension(fileExtension)) {
            return FileValidationResult.error(
                String.format("Inconsistencia detectada: extensión .%s no coincide con el contenido real (%s). " +
                             "Posible archivo malicioso con extensión falsa.", 
                             fileExtension, detectedMimeType)
            );
        }

        // 6. Validar tamaño del archivo
        long fileSize = file.getSize();
        if (fileSize > allowedType.getMaxSize()) {
            return FileValidationResult.error(
                String.format("Archivo demasiado grande. Máximo permitido: %d MB, archivo actual: %.2f MB",
                             allowedType.getMaxSize() / (1024 * 1024),
                             fileSize / (1024.0 * 1024.0))
            );
        }

        // 7. Validación adicional específica por tipo de archivo
        FileValidationResult typeSpecificValidation = validateByFileType(file, allowedType);
        if (!typeSpecificValidation.isValid()) {
            return typeSpecificValidation;
        }

        // 8. Sanitizar nombre de archivo
        String sanitizedFileName = sanitizeFileName(originalFileName);

        return FileValidationResult.success(
            allowedType, detectedMimeType, originalFileName, fileSize, sanitizedFileName
        );
    }

    /**
     * Validar nombre de archivo contra patrones peligrosos
     */
    private FileValidationResult validateFileName(String fileName) {
        if (DANGEROUS_FILE_PATTERN.matcher(fileName).matches()) {
            return FileValidationResult.error(
                "Tipo de archivo ejecutable no permitido por seguridad: " + fileName
            );
        }

        if (DANGEROUS_CHARS_PATTERN.matcher(fileName).find()) {
            return FileValidationResult.error(
                "El nombre del archivo contiene caracteres no permitidos: " + fileName
            );
        }

        if (fileName.length() > 255) {
            return FileValidationResult.error("Nombre de archivo demasiado largo (máximo 255 caracteres)");
        }

        return FileValidationResult.success(null, null, fileName, 0, null);
    }

    /**
     * Detectar extensiones dobles sospechosas (ej: malware.pdf.exe)
     */
    private FileValidationResult validateDoubleExtension(String fileName) {
        if (DOUBLE_EXTENSION_PATTERN.matcher(fileName).matches()) {
            return FileValidationResult.error(
                "Detectada posible extensión doble maliciosa. Archivo rechazado por seguridad: " + fileName
            );
        }

        // Contar puntos para detectar múltiples extensiones
        long dotCount = fileName.chars().filter(ch -> ch == '.').count();
        if (dotCount > 3) { // Permitir hasta 3 puntos (ej: archivo.backup.2023.pdf)
            return FileValidationResult.error(
                "Demasiadas extensiones en el nombre del archivo: " + fileName
            );
        }

        return FileValidationResult.success(null, null, fileName, 0, null);
    }

    /**
     * Validaciones específicas por tipo de archivo
     */
    private FileValidationResult validateByFileType(MultipartFile file, AllowedFileType type) {
        try (InputStream inputStream = file.getInputStream()) {
            // Validar magic numbers (primeros bytes del archivo)
            byte[] header = new byte[32];
            int bytesRead = inputStream.read(header);
            
            if (bytesRead < 4) {
                return FileValidationResult.error("Archivo demasiado pequeño o corrupto");
            }

            return validateMagicNumbers(header, type, file.getOriginalFilename());
            
        } catch (IOException e) {
            return FileValidationResult.error("Error al validar contenido del archivo: " + e.getMessage());
        }
    }

    /**
     * Validar magic numbers (firmas de archivo) para detectar archivos con extensiones falsas
     */
    private FileValidationResult validateMagicNumbers(byte[] header, AllowedFileType type, String fileName) {
        switch (type) {
            case JPEG:
                if (header[0] != (byte) 0xFF || header[1] != (byte) 0xD8) {
                    return FileValidationResult.error("Archivo no es un JPEG válido (magic number incorrecto)");
                }
                break;
            
            case PNG:
                if (header[0] != (byte) 0x89 || header[1] != 'P' || header[2] != 'N' || header[3] != 'G') {
                    return FileValidationResult.error("Archivo no es un PNG válido (magic number incorrecto)");
                }
                break;
            
            case PDF:
                if (header[0] != '%' || header[1] != 'P' || header[2] != 'D' || header[3] != 'F') {
                    return FileValidationResult.error("Archivo no es un PDF válido (magic number incorrecto)");
                }
                break;
            
            case GIF:
                String gifHeader = new String(Arrays.copyOfRange(header, 0, 6));
                if (!gifHeader.equals("GIF87a") && !gifHeader.equals("GIF89a")) {
                    return FileValidationResult.error("Archivo no es un GIF válido (magic number incorrecto)");
                }
                break;
            
            // Para videos MP4, WebM, etc., la validación es más compleja
            // Aquí se puede agregar validación específica si es necesario
            
            default:
                // Para otros tipos, confiar en la detección de MIME type de Tika
                break;
        }

        return FileValidationResult.success(type, null, fileName, 0, null);
    }

    /**
     * Sanitizar nombre de archivo removiendo caracteres peligrosos
     */
    private String sanitizeFileName(String originalFileName) {
        // Remover caracteres peligrosos
        String sanitized = DANGEROUS_CHARS_PATTERN.matcher(originalFileName).replaceAll("_");
        
        // Limitar longitud
        if (sanitized.length() > 200) {
            String extension = getFileExtension(sanitized);
            String baseName = sanitized.substring(0, sanitized.lastIndexOf('.'));
            sanitized = baseName.substring(0, 190) + "." + extension;
        }
        
        // Asegurar que no empiece con punto
        if (sanitized.startsWith(".")) {
            sanitized = "file" + sanitized;
        }

        return sanitized;
    }

    /**
     * Obtener extensión de archivo
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Validar si es una imagen válida
     */
    public boolean isValidImage(MultipartFile file) {
        FileValidationResult result = validateFile(file);
        return result.isValid() && result.getDetectedType() != null && result.getDetectedType().isImage();
    }

    /**
     * Validar si es un documento válido
     */
    public boolean isValidDocument(MultipartFile file) {
        FileValidationResult result = validateFile(file);
        return result.isValid() && result.getDetectedType() != null && result.getDetectedType().isDocument();
    }

    /**
     * Validar si es un video válido
     */
    public boolean isValidVideo(MultipartFile file) {
        FileValidationResult result = validateFile(file);
        return result.isValid() && result.getDetectedType() != null && result.getDetectedType().isVideo();
    }
}
