package com.assembliestore.api.module.product.domain.entity;

import com.google.cloud.firestore.annotation.ServerTimestamp;
import java.util.Date;

public record Specification(
    String id,
    String name, 
    String value,
    boolean visible,
    boolean actived,
    boolean deleted,
    @ServerTimestamp Date createdAt,
    @ServerTimestamp Date updatedAt,
    Date deletedAt
) {}