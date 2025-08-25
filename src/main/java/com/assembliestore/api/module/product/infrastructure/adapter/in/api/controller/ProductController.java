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
import jakarta.servlet.http.HttpServletRequest;
import com.assembliestore.api.common.response.ApiErrorResponse;
import com.assembliestore.api.common.response.ApiSuccessResponse;
import com.assembliestore.api.common.response.ErrorDetail;
import com.assembliestore.api.common.response.ResponseUtil;
import com.assembliestore.api.common.response.TechnicalDetails;
import com.assembliestore.api.config.AppEnvConfig;
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
    private final AppEnvConfig appEnvConfig;

    public ProductController(ProductPort productPort, ProductMapper productMapper, AppEnvConfig appEnvConfig) {
        this.productPort = productPort;
        this.productMapper = productMapper;
        this.appEnvConfig = appEnvConfig;
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
    public ResponseEntity<?> getProductById(@PathVariable String id, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        try {
            String userRole = getUserRole();
            Optional<Product> product = productPort.findProductById(id, userRole);

            if (product.isEmpty()) {
                TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
                ApiErrorResponse error = new ApiErrorResponse("Producto no encontrado", "PRODUCT_NOT_FOUND",
                        java.util.Arrays.asList(new ErrorDetail("product", "not_found")), tech);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            ProductResponseDto productDto = productMapper.toDto(product.get());
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiSuccessResponse<ProductResponseDto> resp = new ApiSuccessResponse<>("Producto obtenido", "PRODUCT_GET_SUCCESS", productDto, tech);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error obteniendo producto", "PRODUCT_GET_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("product", e.getMessage())), tech);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping()
    @Operation(summary = "Obtener todos los productos", description = "Retorna productos filtrados según el rol del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGEMENT') or hasRole('CLIENT')")
    public ResponseEntity<?> getAllProducts(HttpServletRequest request,
                                            @RequestParam(name = "page", required = false) Integer page,
                                            @RequestParam(name = "limit", required = false) Integer limit,
                                            @RequestParam(name = "name", required = false) String name,
                                            @RequestParam(name = "minPrice", required = false) java.math.BigDecimal minPrice,
                                            @RequestParam(name = "maxPrice", required = false) java.math.BigDecimal maxPrice) {
        long start = System.currentTimeMillis();
        try {
            String userRole = getUserRole();
            List<Product> products = productPort.findAllProducts(userRole);

            List<Product> filtered = new java.util.ArrayList<>();
            for (Product p : products) {
                boolean keep = true;
                if (name != null && !name.isBlank()) {
                    String nm = p.getName() == null ? "" : p.getName();
                    if (!nm.toLowerCase().contains(name.toLowerCase())) keep = false;
                }
                if (keep && minPrice != null) {
                    if (p.getPrice() == null || java.math.BigDecimal.valueOf(p.getPrice()).compareTo(minPrice) < 0) keep = false;
                }
                if (keep && maxPrice != null) {
                    if (p.getPrice() == null || java.math.BigDecimal.valueOf(p.getPrice()).compareTo(maxPrice) > 0) keep = false;
                }
                if (keep) filtered.add(p);
            }

            int p = (page == null || page < 1) ? 1 : page;
            int l = (limit == null || limit < 1) ? 10 : limit;

            int totalItems = filtered.size();
            int totalPages = (int) Math.ceil((double) totalItems / l);
            if (totalPages == 0) totalPages = 1;
            int currentPage = Math.min(p, totalPages);
            int fromIndex = (currentPage - 1) * l;
            int toIndex = Math.min(fromIndex + l, totalItems);

            List<ProductResponseDto> pageDtos = new java.util.ArrayList<>();
            if (fromIndex < toIndex) {
                pageDtos = filtered.subList(fromIndex, toIndex).stream().map(productMapper::toDto).collect(Collectors.toList());
            }

            com.assembliestore.api.module.cart.infrastructure.adapter.dto.PaginationDto pagination = new com.assembliestore.api.module.cart.infrastructure.adapter.dto.PaginationDto(
                    totalItems, totalPages, currentPage, currentPage < totalPages, currentPage > 1, l
            );

            java.util.Map<String,Object> data = java.util.Map.of(
                    "items", pageDtos,
                    "pagination", pagination
            );

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiSuccessResponse<java.util.Map<String,Object>> resp = new ApiSuccessResponse<>("Productos obtenidos", "PRODUCT_LIST_SUCCESS", data, tech);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(request, System.currentTimeMillis() - start, appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error obteniendo productos", "PRODUCT_LIST_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("products", e.getMessage())), tech);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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
