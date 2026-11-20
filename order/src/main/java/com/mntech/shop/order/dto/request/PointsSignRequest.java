package com.mntech.shop.order.dto.request;

import lombok.Data;

/**
 * 签到请求
 */
@Data
public class PointsSignRequest {

    /** 签到用户 */
    private String userId;
}