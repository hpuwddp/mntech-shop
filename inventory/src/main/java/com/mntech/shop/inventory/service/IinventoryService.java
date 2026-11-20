package com.mntech.shop.inventory.service;

import com.mntech.shop.inventory.vo.BatchStockRequest;
import com.mntech.shop.pojo.dto.request.SkuStockUpdate;
import com.mntech.shop.pojo.model.SkuModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IinventoryService {
    /**
     * 扣减库存（支付成功后调用）
     *
     * @param productId 商品ID
     * @param skuId SKU ID（可为空）
     * @param quantity 扣减数量
     * @param orderNo 订单号
     */
    boolean  deductStock(String productId, String skuId, Integer quantity, String orderNo);

    /**
     * 回滚库存（订单取消/退款时调用）
     *
     * @param productId 商品ID
     * @param skuId SKU ID（可为空）
     * @param quantity 回滚数量
     * @param orderNo 订单号
     */
    boolean  rollbackStock(String productId, String skuId, Integer quantity, String orderNo);

    /**
     * 检查库存是否充足
     *
     * @param productId 商品ID
     * @param skuId SKU ID（可为空）
     * @param quantity 需要检查的数量
     * @return true-充足，false-不足
     */
    boolean checkStock(String productId, String skuId, Integer quantity);

    /**
     * 获取商品当前库存
     *
     * @param productId 商品ID
     * @param skuId SKU ID（可为空）
     * @return 当前总库存
     */
    Integer getCurrentStock(String productId, String skuId);

    /**
     * 批量查询库存
     * 根据请求中的 productIds 或 skuIds 查询
     */
    List<SkuModel> batchGetStock(BatchStockRequest request);

    /**
     * 批量更新库存
     * @param list
     * @return
     */
    void batchReduceStock(List<SkuStockUpdate> list,String bizNo);
}
