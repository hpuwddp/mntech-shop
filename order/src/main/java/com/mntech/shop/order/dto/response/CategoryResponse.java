package com.mntech.shop.order.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryResponse {
    private String id;

    private String parentId;

    private String parentName;

    private String name;

    private String code;

    private Integer level;

    private Integer sortOrder;

    private String icon;

    private String image;
    private String description;

    private String status;

    private String statusDesc;

    private List<CategoryResponse> children;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
