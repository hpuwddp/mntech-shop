package com.mntech.shop.order.dto.request;

import lombok.Data;

@Data
public class BindRelationRequest {

    /** 当前用户（下级） */
    private String userId;

    /** 推荐人（上级） */
    private String parentId;
}