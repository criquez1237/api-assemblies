package com.assembliestore.api.module.product.infrastructure.adapter.in.api.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.assembliestore.api.module.product.application.port.CategoryPort;
import com.assembliestore.api.module.product.infrastructure.adapter.dto.CategoryRequestDto;
import com.assembliestore.api.module.product.infrastructure.adapter.mapper.CategoryMapperInfras;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/categorie")
@Tag(name = "Categories", description = "Gestión de categorías de productos")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryPort categoryPort;

    public CategoryController(CategoryPort categoryPort) {
        this.categoryPort = categoryPort;
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    @Operation(summary = "Crear nueva categoría", description = "Crea una nueva categoría en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<?> saveCategory(@RequestBody CategoryRequestDto categoryRequestDto) {

        categoryPort.saveCategory(CategoryMapperInfras.toCategorySaveCommand(categoryRequestDto));
        return new ResponseEntity<>("Categoria creada con éxito", HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    @Operation(summary = "Actualizar categoría", description = "Actualiza una categoría existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<?> updateCategory(@RequestBody CategoryRequestDto categoryRequestDto) {

        categoryPort.updateCategory(CategoryMapperInfras.toCategorySaveCommand(categoryRequestDto));
        return new ResponseEntity<>("Categoria actualizada con éxito", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{categoryId}")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    @Operation(summary = "Eliminar categoría", description = "Elimina una categoría del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<?> deleteCategory(@PathVariable String categoryId) {

        categoryPort.deleteCategory(categoryId);
        return new ResponseEntity<>("Categoria eliminada con éxito", HttpStatus.OK);
    }

    @GetMapping("/find/{categoryId}")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    @Operation(summary = "Buscar categoría por ID", description = "Obtiene una categoría específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<?> findCategory(@PathVariable String categoryId) {

        var category = categoryPort.findCategoryById(categoryId);
        return new ResponseEntity<>(category.orElse(null), HttpStatus.OK);
    }

    @GetMapping("/find-all")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'ADMIN', 'CLIENT')")
    @Operation(summary = "Obtener todas las categorías", description = "Obtiene la lista completa de categorías")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorías obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<?> findAllCategories() {
        var categories = categoryPort.findAllCategories();
        if (categories == null) {
            categories = List.of(); // O Collections.emptyList()
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        if (roles.contains("ROLE_CLIENT")) {
            var result = StreamSupport.stream(categories.spliterator(), false)
                    .filter(c -> !c.deleted() && c.visible() && c.actived())
                    .map(c -> Map.of(
                            "name", c.name(),
                            "description", c.description(),
                            "imageUrl", c.imageUrl() != null ? c.imageUrl() : "",
                            "subCategories", c.subCategories() != null ? c.subCategories() : List.of()
                    ))
                    .toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else if (roles.contains("ROLE_MANAGEMENT")) {
            var result = StreamSupport.stream(categories.spliterator(), false)
                    .filter(c -> !c.deleted())
                    .toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else if (roles.contains("ROLE_ADMIN")) {
            var result = StreamSupport.stream(categories.spliterator(), false)
                    .toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
        }
    }

    @PatchMapping("/toggle-active/{categoryId}")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado activo", description = "Activa o desactiva una categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<?> toggleActiveCategory(@PathVariable String categoryId) {

        categoryPort.toggleActiveCategory(categoryId);
        return new ResponseEntity<>("Estado de la categoría actualizado con éxito", HttpStatus.OK);
    }

    @PatchMapping("/toggle-visible/{categoryId}")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    @Operation(summary = "Cambiar visibilidad", description = "Hace visible o invisible una categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Visibilidad actualizada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<?> toggleVisibleCategory(@PathVariable String categoryId) {

        categoryPort.toggleVisibleCategory(categoryId);
        return new ResponseEntity<>("Visibilidad de la categoría actualizada con éxito", HttpStatus.OK);
    }

}
