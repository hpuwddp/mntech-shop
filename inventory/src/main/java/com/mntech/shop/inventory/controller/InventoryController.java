package com.mntech.shop.inventory.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.model.v2.Result;
import com.mntech.shop.inventory.model.StockDeductRecord;
import com.mntech.shop.inventory.service.IStockDeductRecordService;
import com.mntech.shop.inventory.service.IinventoryService;
import com.mntech.shop.inventory.vo.BatchStockRequest;
import com.mntech.shop.inventory.vo.CheckStockRequest;
import com.mntech.shop.inventory.vo.DeductStockRequest;
import com.mntech.shop.inventory.vo.RollbackStockRequest;
import com.mntech.shop.pojo.dto.request.BatchReduceStockRequest;
import com.mntech.shop.pojo.dto.request.SkuStockUpdate;
import com.mntech.shop.pojo.model.SkuModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存管理
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final IinventoryService iinventoryService;
    private final IStockDeductRecordService iStockDeductRecordService;
    /**
     * 查询商品库存
     * GET /api/inventory/stock?productId=xxx&skuId=xxx
     */
    @GetMapping("/stock")
    public Result<Map<String, Object>> getStock(@RequestParam String productId,@RequestParam(required = false) String skuId) {

       // log.info("查询库存: productId={}, skuId={}", productId, skuId);

        Integer stock = iinventoryService.getCurrentStock(productId, skuId);

        Map<String, Object> data = new HashMap<>();
        data.put("productId", productId);
        data.put("skuId", skuId);
        data.put("stock", stock);

        return Result.success(data);
    }
    /**
     * 批量查询库存
     * POST /api/inventory/stock/batch
     * 支持按商品ID列表或SKU ID列表查询
     */
    @PostMapping("/stock/batch")
    public Result<Map<String, Object>> batchGetStock(@RequestBody BatchStockRequest request) {

       // log.info("批量查询库存: productIds={}, skuIds={}", request.getProductIds(), request.getSkuIds());

        List<SkuModel> skuList =  iinventoryService.batchGetStock(request);
        Map<String, Object> data = new HashMap<>();
        data.put("code", 200);
        data.put("list", skuList);
        return Result.success(data);
    }


    /**
     * 批量扣减库存
     * POST /api/inventory/stock/batch
     * 支持按商品ID列表或SKU ID列表查询
     */
    @PostMapping("/stock/deduct")
    public Result<?> batchDeductStock(@RequestBody @Valid BatchReduceStockRequest request) {

        System.out.println("batchDeductStock==");
        // log.info("批量查询库存: productIds={}, skuIds={}", request.getProductIds(), request.getSkuIds());
        try{
            iinventoryService.batchReduceStock(request.getItems(),request.getBizNo());
            return Result.success();
        }
        catch (Exception ex){

            return Result.failure("库存扣减失败");
        }
    }



    /**
     * 检查库存是否充足
     * POST /api/inventory/check
     */
    @PostMapping("/check")
    public Result<Map<String, Object>> checkStock(@RequestBody CheckStockRequest request) {

       // log.info("检查库存: productId={}, skuId={}, quantity={}",
             //   request.getProductId(), request.getSkuId(), request.getQuantity());
        Map<String, Object> data = new HashMap<>();
        boolean enough = iinventoryService.checkStock(
                request.getProductId(),
                request.getSkuId(),
                request.getQuantity()
        );
        if (enough){
            data.put("code", 20000);
            data.put("msg", "库存充足");
        }
        else
        {
            data.put("code", 40000);
            data.put("msg", "库存不足或不存在");
        }
        return Result.success(data);
    }

    @PostMapping("/deduct")
    public Result<Map<String, Object>> deductStock(@RequestBody DeductStockRequest request) {
//        log.info("管理后台扣减库存: productId={}, skuId={}, quantity={}, orderNo={}",
//                request.getProductId(), request.getSkuId(), request.getQuantity(), request.getOrderNo());
        Map<String, Object> data = new HashMap<>();
        boolean success = iinventoryService.deductStock(
                request.getProductId(),
                request.getSkuId(),
                request.getQuantity(),
                request.getOrderNo()
        );

        if (success){
            data.put("code", 20000);
            data.put("msg", "库存扣减成功");
        }
        else
        {
            data.put("code", 40000);
            data.put("msg", "库存扣减失败,请查看日志...");
        }
        return Result.success(data);
    }

    @PostMapping("/rollback")
    public Result<Map<String, Object>> rollbackStock(@RequestBody RollbackStockRequest request) {
//        log.info("管理后台回滚库存: productId={}, skuId={}, quantity={}, orderNo={}",
//                request.getProductId(), request.getSkuId(), request.getQuantity(), request.getOrderNo());
        Map<String, Object> data = new HashMap<>();
        boolean success = iinventoryService.rollbackStock(
                request.getProductId(),
                request.getSkuId(),
                request.getQuantity(),
                request.getOrderNo()
        );

        if (success){
            data.put("code", 20000);
            data.put("msg", "库存回滚成功");
        }
        else
        {
            data.put("code", 40000);
            data.put("msg", "库存回滚失败,请查看日志...");
        }
        return Result.success(data);
    }
}
