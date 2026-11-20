package com.mntech.shop.common.Feign;

import com.mntech.shop.pojo.dto.request.BatchReduceStockRequest;
import com.mntech.shop.pojo.dto.request.BatchStockRequest;
import com.mntech.shop.pojo.dto.request.SkuStockUpdate;
import com.mntech.shop.pojo.dto.response.Result;
import com.mntech.shop.pojo.model.SkuModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryFeignClient {
    @PostMapping("/deduct")
    Result<Void> deductStock(@RequestParam("productId") String productId,
                             @RequestParam("skuId") String skuId,
                             @RequestParam("quantity") Integer quantity,
                             @RequestParam("orderNo") String orderNo);

    @PostMapping("/rollback")
    Result<Void> rollbackStock(@RequestParam("productId") String productId,
                               @RequestParam("skuId") String skuId,
                               @RequestParam("quantity") Integer quantity,
                               @RequestParam("orderNo") String orderNo);

    /**
     * 批量查询SKU信息
     * 支持按商品ID列表或SKU ID列表查询
     */
    @PostMapping("/stock/batch")
    Result<Map<String, Object>> batchGetStock(@RequestBody BatchStockRequest request);

    /**
     * 批量扣减sku库存
     */
    @PostMapping("/stock/deduct")
    Result<Map<String,Object>> batchDeductStock(@RequestBody BatchReduceStockRequest request);


}
