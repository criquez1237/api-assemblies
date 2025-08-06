package com.assembliestore.api.module.product.domain.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.assembliestore.api.common.type.FileFormat;
import com.google.cloud.firestore.annotation.ServerTimestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor  // OBLIGATORIO para Firebase
@AllArgsConstructor // Para que funcione el Builder
public class Category {

    private String id;
    private String name;
    private String description;
    private FileFormat imageUrl;
    @Builder.Default
    private List<SubCategory> subCategories = new ArrayList<>();
    private boolean actived;
    private boolean deleted;
    private boolean visible;
    
    @ServerTimestamp
    private Date createdAt;
    
    @ServerTimestamp
    private Date updatedAt;
    
    private Date deletedAt;

    public void markToogleActive() {
        this.actived = !this.actived;
        this.updatedAt = new Date();
    }

    public void markToogleVisible() {
        this.visible = !this.visible;
        this.updatedAt = new Date();
    }

    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = new Date();
    }

    public void refreshUpdatedAt() {
        this.updatedAt = new Date();
    }
}
