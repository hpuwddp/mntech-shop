package com.mntech.shop.order.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalApplyRequest {

    /** 提现用户 */
    private String userId;

    /** 提现金额 */
    private BigDecimal amount;
}