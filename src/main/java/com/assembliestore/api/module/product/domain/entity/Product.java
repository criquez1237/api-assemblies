package com.assembliestore.api.module.product.domain.entity;

import java.util.Date;
import java.util.List;

import com.google.cloud.firestore.annotation.ServerTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String id;
    private String name;
    private String description;
    private List<Specification> specifications;
    private String brandName;
    @Builder.Default
    private Double price = 0.0;
    private List<Gallery> gallery;
    private String subCategoryId;
    @Builder.Default
    private Integer stockQuantity = 0;
    @Builder.Default
    private boolean actived = true;
    @Builder.Default
    private boolean visible = true;
    @Builder.Default
    private boolean deleted = false;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;
    private Date deletedAt;

    public void refreshUpdatedAt() {
        this.updatedAt = new Date();
    }
    // Métodos para galería
    public void addGalleryImage(Gallery image) {
        if (this.gallery == null) this.gallery = new java.util.ArrayList<>();
        this.gallery.add(image);
        refreshUpdatedAt();
    }
    public void removeGalleryImage(String imageId) {
        if (this.gallery != null) {
            this.gallery.removeIf(img -> img.id().equals(imageId));
            refreshUpdatedAt();
        }
    }
    public void setGalleryImageVisible(String imageId, Boolean visible) {
        if (this.gallery != null) {
            this.gallery = this.gallery.stream()
                .map(img -> img.id().equals(imageId) ? new Gallery(img.id(), img.imageUrl(), img.description(), visible, img.actived(), img.deleted(), img.createdAt(), img.updatedAt(), img.deletedAt()) : img)
                .toList();
            refreshUpdatedAt();
        }
    }
    public void setGalleryImageActived(String imageId, Boolean actived) {
        if (this.gallery != null) {
            this.gallery = this.gallery.stream()
                .map(img -> img.id().equals(imageId) ? new Gallery(img.id(), img.imageUrl(), img.description(), img.visible(), actived, img.deleted(), img.createdAt(), img.updatedAt(), img.deletedAt()) : img)
                .toList();
            refreshUpdatedAt();
        }
    }
    public void setGalleryImageDeleted(String imageId, Boolean deleted) {
        if (this.gallery != null) {
            this.gallery = this.gallery.stream()
                .map(img -> img.id().equals(imageId) ? new Gallery(img.id(), img.imageUrl(), img.description(), img.visible(), img.actived(), deleted, img.createdAt(), img.updatedAt(), deleted ? new java.util.Date() : img.deletedAt()) : img)
                .toList();
            refreshUpdatedAt();
        }
    }
    // Métodos para especificaciones
    public void addSpecification(Specification spec) {
        if (this.specifications == null) this.specifications = new java.util.ArrayList<>();
        this.specifications.add(spec);
        refreshUpdatedAt();
    }
    public void removeSpecification(String specId) {
        if (this.specifications != null) {
            this.specifications.removeIf(spec -> spec.id().equals(specId));
            refreshUpdatedAt();
        }
    }
    public void setSpecificationVisible(String specId, Boolean visible) {
        if (this.specifications != null) {
            this.specifications = this.specifications.stream()
                .map(spec -> spec.id().equals(specId) ? new Specification(spec.id(), spec.name(), spec.value(), visible, spec.actived(), spec.deleted(), spec.createdAt(), spec.updatedAt(), spec.deletedAt()) : spec)
                .toList();
            refreshUpdatedAt();
        }
    }
    public void setSpecificationActived(String specId, Boolean actived) {
        if (this.specifications != null) {
            this.specifications = this.specifications.stream()
                .map(spec -> spec.id().equals(specId) ? new Specification(spec.id(), spec.name(), spec.value(), spec.visible(), actived, spec.deleted(), spec.createdAt(), spec.updatedAt(), spec.deletedAt()) : spec)
                .toList();
            refreshUpdatedAt();
        }
    }
    public void setSpecificationDeleted(String specId, Boolean deleted) {
        if (this.specifications != null) {
            this.specifications = this.specifications.stream()
                .map(spec -> spec.id().equals(specId) ? new Specification(spec.id(), spec.name(), spec.value(), spec.visible(), spec.actived(), deleted, spec.createdAt(), spec.updatedAt(), deleted ? new java.util.Date() : spec.deletedAt()) : spec)
                .toList();
            refreshUpdatedAt();
        }
    }
}
