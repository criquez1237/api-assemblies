package com.assembliestore.api.module.product.infrastructure.adapter.in.api.controller;

import com.assembliestore.api.common.interfaces.SuccessfulResponse;
import com.assembliestore.api.module.product.application.dto.request.ProductBatchRequestDto;
import com.assembliestore.api.module.product.application.dto.request.ProductRequestDto;
import com.assembliestore.api.module.product.application.dto.response.ProductResponseDto;
import com.assembliestore.api.module.product.application.mapper.ProductMapper;
import com.assembliestore.api.module.product.application.port.ProductPort;
import com.assembliestore.api.module.product.domain.entity.Product;
import com.assembliestore.api.module.product.domain.entity.Gallery;
import com.assembliestore.api.module.product.domain.entity.Specification;
import com.assembliestore.api.module.product.application.dto.request.GalleryActionRequestDto;
import com.assembliestore.api.module.product.application.dto.request.SpecificationActionRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("products")
@Tag(name = "Product Management", description = "Endpoints para gestión de productos")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductPort productPort;
    private final ProductMapper productMapper;

    public ProductController(ProductPort productPort, ProductMapper productMapper) {
        this.productPort = productPort;
        this.productMapper = productMapper;
    }

    /**
     * Extrae el rol del usuario autenticado desde el contexto de seguridad
     */
    private String getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getAuthorities() != null) {
            // Buscar el rol más alto en orden de prioridad: ADMIN > MANAGEMENT > CLIENT
            if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
                return "ADMIN";
            } else if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MANAGEMENT"))) {
                return "MANAGEMENT";
            } else if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CLIENT"))) {
                return "CLIENT";
            }
        }
        
        // Por defecto, rol CLIENT si no se encuentra ninguno
        return "CLIENT";
    }

    /*@PostMapping("/upsert")
    @Operation(summary = "Crear o actualizar un producto", description = "Si viene ID es update, si no viene ID es insert. Valida nombres duplicados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto guardado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Nombre duplicado"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<SuccessfulResponse> upsertProduct(
            @RequestBody ProductRequestDto productRequest) {
        try {
            Product product = productMapper.toEntity(productRequest);
            productPort.upsertProduct(product);
            
            String message = product.getId() != null && !product.getId().isEmpty() 
                ? "Producto actualizado exitosamente" 
                : "Producto creado exitosamente";
                
            return ResponseEntity.ok(new SuccessfulResponse(message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SuccessfulResponse("Error: " + e.getMessage()));
        }
    }*/

    @PostMapping("/upsert")
    @Operation(summary = "Crear o actualizar múltiples productos", description = "Procesa múltiples productos en una sola operación. Mínimo 1 producto requerido.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Productos guardados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o lista vacía"),
        @ApiResponse(responseCode = "409", description = "Nombre duplicado"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<SuccessfulResponse> upsertProducts(
            @RequestBody ProductBatchRequestDto batchRequest) {
        try {
            if (batchRequest.getProducts() == null || batchRequest.getProducts().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new SuccessfulResponse("Error: Se requiere al menos un producto"));
            }
            
            List<Product> products = batchRequest.getProducts().stream()
                .map(productMapper::toEntity)
                .collect(Collectors.toList());
                
            productPort.upsertProducts(products);
            
            return ResponseEntity.ok(new SuccessfulResponse(
                "Se procesaron " + products.size() + " productos exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SuccessfulResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Retorna datos filtrados según el rol del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT') or hasRole('CLIENT')")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        try {
            String userRole = getUserRole();
            Optional<Product> product = productPort.findProductById(id, userRole);
            
            if (product.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new SuccessfulResponse("Producto no encontrado"));
            }
            
            ProductResponseDto productDto = productMapper.toDto(product.get());
            return ResponseEntity.ok(productDto);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SuccessfulResponse("Error: " + e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Obtener todos los productos", description = "Retorna productos filtrados según el rol del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT') or hasRole('CLIENT')")
    public ResponseEntity<?> getAllProducts() {
        try {
            String userRole = getUserRole();
            List<Product> products = productPort.findAllProducts(userRole);
            
            List<ProductResponseDto> productDtos = products.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(productDtos);
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new SuccessfulResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/gallery")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<SuccessfulResponse> addGalleryImage(@PathVariable String id, @RequestBody Gallery image) {
        Gallery imageWithUuid = new Gallery(
            java.util.UUID.randomUUID().toString(),
            image.imageUrl(),
            image.description(),
            image.visible(),
            image.actived(),
            image.deleted(),
            image.createdAt(),
            image.updatedAt(),
            image.deletedAt()
        );
        productPort.addGalleryImage(id, imageWithUuid);
        return ResponseEntity.ok(new SuccessfulResponse("Imagen agregada a la galería"));
    }

    @DeleteMapping("/{id}/gallery/{imageId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<SuccessfulResponse> removeGalleryImage(@PathVariable String id, @PathVariable String imageId) {
        productPort.removeGalleryImage(id, imageId);
        return ResponseEntity.ok(new SuccessfulResponse("Imagen eliminada de la galería"));
    }

    @PatchMapping("/{id}/gallery/{imageId}/state")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<SuccessfulResponse> setGalleryImageState(@PathVariable String id, @PathVariable String imageId, @RequestBody GalleryActionRequestDto dto) {
        productPort.setGalleryImageState(id, imageId, dto.visible, dto.actived, dto.deleted);
        return ResponseEntity.ok(new SuccessfulResponse("Estado de la imagen actualizado"));
    }

    @PostMapping("/{id}/specification")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<SuccessfulResponse> addSpecification(@PathVariable String id, @RequestBody Specification spec) {
        Specification specWithUuid = new Specification(
            java.util.UUID.randomUUID().toString(),
            spec.name(),
            spec.value(),
            spec.visible(),
            spec.actived(),
            spec.deleted(),
            spec.createdAt(),
            spec.updatedAt(),
            spec.deletedAt()
        );
        productPort.addSpecification(id, specWithUuid);
        return ResponseEntity.ok(new SuccessfulResponse("Especificación agregada"));
    }

    @DeleteMapping("/{id}/specification/{specId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<SuccessfulResponse> removeSpecification(@PathVariable String id, @PathVariable String specId) {
        productPort.removeSpecification(id, specId);
        return ResponseEntity.ok(new SuccessfulResponse("Especificación eliminada"));
    }

    @PatchMapping("/{id}/specification/{specId}/state")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT')")
    public ResponseEntity<SuccessfulResponse> setSpecificationState(@PathVariable String id, @PathVariable String specId, @RequestBody SpecificationActionRequestDto dto) {
        productPort.setSpecificationState(id, specId, dto.visible, dto.actived, dto.deleted);
        return ResponseEntity.ok(new SuccessfulResponse("Estado de la especificación actualizado"));
    }
}
