package com.mntech.shop.inventory.service.impl;

import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.inventory.dao.ProductMapper;
import com.mntech.shop.inventory.dao.SkuMapper;
import com.mntech.shop.inventory.dao.StockDeductRecordMapper;
import com.mntech.shop.inventory.model.StockDeductRecord;
import com.mntech.shop.inventory.service.IinventoryService;
import com.mntech.shop.inventory.vo.BatchStockRequest;
import com.mntech.shop.pojo.dto.request.SkuStockUpdate;
import com.mntech.shop.pojo.model.ProductModel;
import com.mntech.shop.pojo.model.SkuModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService implements IinventoryService {
    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final StockDeductRecordMapper recordMapper;
    /**
     * 扣减库存（支付成功后调用）
     * 减少总库存，增加已售库存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(String productId, String skuId, Integer quantity, String orderNo) {
       // log.info("扣减库存: productId={}, skuId={}, quantity={}, orderNo={}", productId, skuId, quantity, orderNo);

        if (skuId != null && !skuId.isEmpty()) {
            SkuModel sku = skuMapper.selectById(skuId);
            if (sku == null) {
                log.warn("SKU不存在: {}", skuId);
                return false;
            }
            if (sku.getTotalStock() < quantity) {
                log.warn("库存不足: skuId={}, totalStock={}, quantity={}", skuId, sku.getTotalStock(), quantity);
                return false;
            }

            int affected = skuMapper.deductStock(skuId, quantity, sku.getVersion());
            if (affected == 0) {
                log.warn("库存扣减失败(乐观锁冲突): skuId={}, quantity={}", skuId, quantity);
                return false;
            }

            // 记录日志...
          //  log.info("SKU库存扣减成功: skuId={}", skuId);
            return true;

        } else {
            // SPU级别库存扣减...
            ProductModel product = productMapper.selectById(productId);
            if (product == null) {
                log.warn("商品不存在: {}", productId);
                return false;
            }
            if (product.getTotalStock() < quantity) {
                log.warn("库存不足: productId={}, totalStock={}, quantity={}", productId, product.getTotalStock(), quantity);
                return false;
            }

            int affected = productMapper.deductStock(productId, quantity, product.getVersion());
            if (affected == 0) {
                log.warn("库存扣减失败(乐观锁冲突): productId={}, quantity={}", productId, quantity);
                return false;
            }

            // 记录日志...
          //  log.info("商品库存扣减成功: productId={}", productId);
            return true;
        }
    }

    /**
     * 回滚库存（订单取消/退款时调用）
     * 增加总库存，减少已售库存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean  rollbackStock(String productId, String skuId, Integer quantity, String orderNo) {
       // log.info("回滚库存: productId={}, skuId={}, quantity={}, orderNo={}", productId, skuId, quantity, orderNo);

        if (skuId != null && !skuId.isEmpty()) {
            // ========== SKU级别库存回滚 ==========
            SkuModel sku = skuMapper.selectById(skuId);
            if (sku == null) {
                throw new BusinessException("SKU不存在: " + skuId);
            }
            if (sku.getSoldStock() < quantity) {
                log.warn("已售库存不足，无法回滚: skuId={}, soldStock={}, quantity={}", skuId, sku.getSoldStock(), quantity);
                return false;
            }

            int affected = skuMapper.rollbackStock(skuId, quantity, sku.getVersion());
            if (affected == 0) {
                throw new BusinessException("库存回滚失败，请重试");
            }

//            saveStockLog(orderNo, productId, skuId, StockChangeType.ROLLBACK,
//                    quantity, sku.getTotalStock(), sku.getTotalStock() + quantity,
//                    sku.getSoldStock(), sku.getSoldStock() - quantity);

          //  log.info("SKU库存回滚成功: skuId={}, 当前库存={}", skuId, sku.getTotalStock() + quantity);
            return true;

        } else {
            // ========== SPU级别库存回滚 ==========
            ProductModel product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException("商品不存在: " + productId);
            }
            if (product.getSoldStock() < quantity) {
                log.warn("已售库存不足，无法回滚: productId={}, soldStock={}, quantity={}", productId, product.getSoldStock(), quantity);
                return false;
            }

            int affected = productMapper.rollbackStock(productId, quantity, product.getVersion());
            if (affected == 0) {
                throw new BusinessException("库存回滚失败，请重试");
            }

//            saveStockLog(orderNo, productId, null, StockChangeType.ROLLBACK,
//                    quantity, product.getTotalStock(), product.getTotalStock() + quantity,
//                    product.getSoldStock(), product.getSoldStock() - quantity);

         //   log.info("商品库存回滚成功: productId={}, 当前库存={}", productId, product.getTotalStock() + quantity);
            return true;
        }
    }

    /**
     * 检查库存是否充足
     */
    @Override
    public boolean checkStock(String productId, String skuId, Integer quantity) {
      //  log.debug("检查库存: productId={}, skuId={}, quantity={}", productId, skuId, quantity);

        if (skuId != null && !skuId.isEmpty()) {
            SkuModel sku = skuMapper.selectById(skuId);
            if (sku == null) {
                return false;
            }
            return sku.getTotalStock() >= quantity;
        } else {
            ProductModel product = productMapper.selectById(productId);
            if (product == null) {
                return false;
            }
            return product.getTotalStock() >= quantity;
        }
    }

    /**
     * 获取商品当前库存
     */
    @Override
    public Integer getCurrentStock(String productId, String skuId) {
       // log.debug("获取当前库存: productId={}, skuId={}", productId, skuId);

        if (skuId != null && !skuId.isEmpty()) {
            SkuModel sku = skuMapper.selectById(skuId);
            return sku != null ? sku.getTotalStock() : 0;
        } else {
            ProductModel product = productMapper.selectById(productId);
            return product != null ? product.getTotalStock() : 0;
        }
    }

    @Override
    public List<SkuModel> batchGetStock(BatchStockRequest request) {

        List<SkuModel> skuModels = new ArrayList<>();

        // 1. 按SKU ID精确查询（优先级高）
        if (request.getSkuIds() != null && !request.getSkuIds().isEmpty()) {
            skuModels = skuMapper.selectBatchIds(request.getSkuIds());
        }
        // 2. 按商品ID查询该商品下所有SKU
        else if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            for (String productId : request.getProductIds()) {
                // 查询该商品下的所有SKU
                List<SkuModel> skus = skuMapper.selectByProductId(productId);
                skuModels.addAll(skus);
            }
        }
        else {
            log.warn("批量查询库存: productIds和skuIds都为空");
            return new ArrayList<>();
        }


        return skuModels;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchReduceStock(List<SkuStockUpdate> list, String bizNo) {
        System.out.println("batchDeductStock----");
        if(recordMapper.selectByBizNo(bizNo)!=null){
            System.out.println("幂等命中，bizNo="+bizNo);
            return;
        }
        int effectRows= skuMapper.batchReduceStock(list);
        if (effectRows!=list.size()){
            System.out.println("effectRows!=list.size()");
            throw new RuntimeException("库存扣减失败：期望扣减 " + list.size() + " 条，实际更新 " + effectRows + " 条");
        }
        // 写记录（MP insert，jsonb 由 TypeHandler 自动处理）
        StockDeductRecord record = new StockDeductRecord();
        record.setBizNo(bizNo);
        record.setItemCount(list.size());
        record.setStatus(1);
        recordMapper.insert(record);
    }
}
