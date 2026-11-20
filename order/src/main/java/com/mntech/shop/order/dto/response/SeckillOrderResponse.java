package com.mntech.shop.order.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderResponse {
    private String orderId;
    private String seckillProductId;
    private String productName;
    private String productImage;
    private BigDecimal seckillPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
    private String status;
    private String statusDesc;
    private LocalDateTime createdAt;
    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;
    // 支付参数
    private String timeStamp;
    private String nonceStr;
    private String packageStr;
    private String signType;
    private String paySign;

    private Long remainingPayTime;
}
