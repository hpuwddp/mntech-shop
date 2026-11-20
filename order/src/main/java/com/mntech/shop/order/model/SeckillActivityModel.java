package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("seckill_activity")
public class SeckillActivityModel {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private String status; // PENDING(待审核), APPROVED(已通过), REJECTED(已驳回), ONGOING(进行中), ENDED(已结束)

    @TableField("created_by")
    private String createdBy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    // ========== 非表字段 ==========

    @TableField(exist = false)
    private String statusDesc;
    @TableField(exist = false)
    private Integer productCount;

    @TableField(exist = false)
    private List<SeckillProductModel> products;

    public String getStatusDesc() {
        if (status == null) return "";
        switch (status) {
            case "PENDING":
                return "待审核";
            case "APPROVED":
                return "已通过";
            case "REJECTED":
                return "已驳回";
            case "ONGOING":
                return "进行中";
            case "ENDED":
                return "已结束";
            default:
                return status;
        }
    }

    public Boolean getIsOngoing() {
        return "ONGOING".equals(status);
    }

    public Boolean getIsEnded() {
        return "ENDED".equals(status);
    }
}
