package com.mntech.shop.pojo.dto.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchReduceStockRequest {
    @NotBlank(message = "幂等键不能为空")
    private String bizNo;

    @NotEmpty(message = "扣减列表不能为空")
    @Valid
    private List<SkuStockUpdate> items;
}
