package com.assembliestore.api.module.product.domain.entity;

import java.util.Date;
import com.assembliestore.api.common.type.FileFormat;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor // OBLIGATORIO para Firebase
@AllArgsConstructor // Para que funcione el Builder
public class SubCategory {

    private String id;
    private String name;
    private String description;
    private FileFormat imageUrl;
    private boolean actived;
    private boolean deleted;
    private boolean visible;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
    private String categoryId;


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
    
    public boolean isDeleted() {
        return deleted;
    }
    public boolean isVisible() {
        return visible;
    }
    public boolean isActived() {
        return actived;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public FileFormat getImageUrl() {
        return imageUrl;
    }
    public String getCategoryId() {
        return categoryId;
    }
}
