package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水
 */
@Data
@Builder
public class PointsRecordResponse {

    private String id;

    /** 类型：SIGN/SIGN_BONUS */
    private String changeType;

    /** 类型说明 */
    private String changeTypeDesc;

    /** 变动积分（正数） */
    private Integer points;

    /** 备注 */
    private String remark;

    private LocalDateTime createdAt;
}