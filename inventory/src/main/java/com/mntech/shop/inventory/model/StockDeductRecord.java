package com.mntech.shop.inventory.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("stock_deduct_record")
public class StockDeductRecord {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /** 幂等键（订单号/业务单号） */
    private String bizNo;
    /** 本次扣减 SKU 条数 */
    private Integer itemCount;
    /** 扣减明细快照 JSON */
    private String detailJson;
    /** 状态：1-成功 0-失败 */
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
