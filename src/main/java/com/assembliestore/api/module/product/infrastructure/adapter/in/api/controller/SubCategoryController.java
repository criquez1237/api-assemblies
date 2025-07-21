package com.assembliestore.api.module.product.infrastructure.adapter.in.api.controller;

import com.assembliestore.api.module.product.application.port.SubCategoryPort;
import com.assembliestore.api.module.product.domain.entity.SubCategory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/subcategories")
// Puedes agregar anotaciones Swagger aquí si lo deseas
public class SubCategoryController {
    private final SubCategoryPort subCategoryPort;

    public SubCategoryController(SubCategoryPort subCategoryPort) {
        this.subCategoryPort = subCategoryPort;
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    public ResponseEntity<?> saveSubCategory(@RequestBody SubCategory subCategory) {
        subCategoryPort.saveSubCategory(subCategory);
        return new ResponseEntity<>("Subcategoría creada con éxito", HttpStatus.CREATED);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    public ResponseEntity<?> updateSubCategory(@RequestBody SubCategory subCategory) {
        subCategoryPort.updateSubCategory(subCategory);
        return new ResponseEntity<>("Subcategoría actualizada con éxito", HttpStatus.OK);
    }

    @DeleteMapping("/delete/{subCategoryId}")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteSubCategory(@PathVariable String subCategoryId) {
        subCategoryPort.deleteSubCategory(subCategoryId);
        return new ResponseEntity<>("Subcategoría eliminada con éxito", HttpStatus.OK);
    }

    @GetMapping("/find/{subCategoryId}")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'ADMIN', 'CLIENT')")
    public ResponseEntity<?> findSubCategory(@PathVariable String subCategoryId) {
        var subCategory = subCategoryPort.findSubCategoryById(subCategoryId);
        return new ResponseEntity<>(subCategory.orElse(null), HttpStatus.OK);
    }

    @GetMapping("/find-all")
    @PreAuthorize("hasAnyRole('MANAGEMENT', 'ADMIN', 'CLIENT')")
    public ResponseEntity<?> findAllSubCategories() {
        var subCategories = subCategoryPort.findAllSubCategories();
        if (subCategories == null) {
            subCategories = List.of();
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();
        if (roles.contains("ROLE_CLIENT")) {
            var result = StreamSupport.stream(subCategories.spliterator(), false)
                    .filter(sc -> !sc.isDeleted() && sc.isVisible() && sc.isActived())
                    .map(sc -> Map.of(
                            "name", sc.getName(),
                            "description", sc.getDescription(),
                            "imageUrl", sc.getImageUrl() != null ? sc.getImageUrl() : "",
                            "categoryId", sc.getCategoryId() != null ? sc.getCategoryId() : ""
                    ))
                    .toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else if (roles.contains("ROLE_MANAGEMENT")) {
            var result = StreamSupport.stream(subCategories.spliterator(), false)
                    .filter(sc -> !sc.isDeleted())
                    .toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else if (roles.contains("ROLE_ADMIN")) {
            var result = StreamSupport.stream(subCategories.spliterator(), false)
                    .toList();
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
        }
    }

    @PatchMapping("/toggle-active/{subCategoryId}")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    public ResponseEntity<?> toggleActiveSubCategory(@PathVariable String subCategoryId) {
        subCategoryPort.toggleActiveSubCategory(subCategoryId);
        return new ResponseEntity<>("Estado de la subcategoría actualizado con éxito", HttpStatus.OK);
    }

    @PatchMapping("/toggle-visible/{subCategoryId}")
    @PreAuthorize("hasRole('MANAGEMENT') or hasRole('ADMIN')")
    public ResponseEntity<?> toggleVisibleSubCategory(@PathVariable String subCategoryId) {
        subCategoryPort.toggleVisibleSubCategory(subCategoryId);
        return new ResponseEntity<>("Visibilidad de la subcategoría actualizada con éxito", HttpStatus.OK);
    }
}
