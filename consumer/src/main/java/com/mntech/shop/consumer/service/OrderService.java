package com.mntech.shop.consumer.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mntech.shop.consumer.enums.OrderStatus;
import com.mntech.shop.consumer.mapper.OrderItemMapper;
import com.mntech.shop.consumer.mapper.OrderMapper;
import com.mntech.shop.consumer.mapper.SeckillOrderMapper;
import com.mntech.shop.consumer.mapper.SkuMapper;
import com.mntech.shop.consumer.model.SeckillOrderModel;
import com.mntech.shop.pojo.dto.request.SkuStockUpdate;
import com.mntech.shop.pojo.model.OrderItemModel;
import com.mntech.shop.pojo.model.OrderModel;
import com.mntech.shop.pojo.model.SkuModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService  extends ServiceImpl<OrderMapper, OrderModel> {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private SkuMapper skuMapper;

    @Transactional(rollbackFor = Exception.class)
    public void deductStockForOrder(OrderModel order) {
        String orderNo = order.getOrderNo();
        log.info("开始扣减订单库存: orderNo={}", orderNo);

        // 1. 查询订单明细
        List<OrderItemModel> orderItems = orderItemMapper.selectByOrderNo(orderNo);
        if (orderItems == null || orderItems.isEmpty()) {
            log.warn("订单明细为空: orderNo={}", orderNo);
            return;
        }

        // 2. 按 SKU 汇总数量（同一个 SKU 可能在不同明细中出现多次）
        java.util.Map<String, Integer> skuQuantityMap = orderItems.stream()
                .filter(item -> item.getSkuId() != null && !item.getSkuId().isEmpty())
                .collect(Collectors.toMap(
                        OrderItemModel::getSkuId,
                        OrderItemModel::getQuantity,
                        Integer::sum
                ));

        if (skuQuantityMap.isEmpty()) {
            log.warn("订单中没有SKU商品: orderNo={}", orderNo);
            return;
        }

        // 3. 查询 SKU 最新数据（获取 version）
        List<String> skuIds = new ArrayList<>(skuQuantityMap.keySet());
        List<SkuModel> skuList = skuMapper.selectBatchIds(skuIds);

        // 4. 构建批量更新参数
        List<SkuStockUpdate> updateList = new ArrayList<>();
        for (SkuModel sku : skuList) {
            Integer quantity = skuQuantityMap.get(sku.getId());
            if (quantity == null || quantity <= 0) {
                continue;
            }

            // 检查库存是否充足
            if (sku.getAvailableStock() == null || sku.getAvailableStock() < quantity) {
                log.error("库存不足: orderNo={}, skuId={}, available={}, need={}",
                        orderNo, sku.getId(), sku.getAvailableStock(), quantity);
               // throw new BusinessException("SKU库存不足: " + sku.getSkuName());
            }

            updateList.add(new SkuStockUpdate(
                    sku.getId(),
                    quantity,
                    sku.getVersion()
            ));
        }

        if (updateList.isEmpty()) {
            log.warn("没有需要扣减库存的SKU: orderNo={}", orderNo);
            return;
        }

        // 5. 批量扣减库存（乐观锁）
        int affectedRows = skuMapper.batchReduceStock(updateList);

        // 6. 检查更新结果
        if (affectedRows != updateList.size()) {
            log.error("批量扣减库存失败: orderNo={}, 预期{}个, 实际{}个",
                    orderNo, updateList.size(), affectedRows);
          //  throw new BusinessException("库存扣减失败");
        }

        // 7. 更新订单状态为已扣库存（可选）
        // orderMapper.updateStockDeducted(orderNo);

        log.info("订单库存扣减成功: orderNo={}, 更新{}个SKU", orderNo, affectedRows);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrder(OrderModel order) {
        String orderNo = order.getOrderNo();
        log.info("开始取消超时订单: orderNo={}, userId={}", orderNo, order.getUserId());

        try {
            // 1. 再次检查订单状态（防止并发修改）
            OrderModel latestOrder = orderMapper.selectByOrderNo(orderNo);
            if (latestOrder == null) {
                log.warn("订单不存在: orderNo={}", orderNo);
                return;
            }

            // 如果不是待支付状态，不需要取消
            if (!OrderStatus.PENDING.name().equals(latestOrder.getStatus())) {
                log.info("订单状态不是待支付，跳过取消: orderNo={}, status={}",
                        orderNo, latestOrder.getStatus());
                return;
            }

            // 2. 查询订单明细
            List<OrderItemModel> orderItems = orderItemMapper.selectByOrderNo(orderNo);
            if (orderItems == null || orderItems.isEmpty()) {
                log.warn("订单明细为空: orderNo={}", orderNo);
                // 直接更新订单状态为已取消
                updateOrderStatus(orderNo, OrderStatus.CANCELLED);
                return;
            }

            // 3. 按 SKU 汇总数量
            Map<String, Integer> skuQuantityMap = orderItems.stream()
                    .filter(item -> item.getSkuId() != null && !item.getSkuId().isEmpty())
                    .collect(Collectors.toMap(
                            OrderItemModel::getSkuId,
                            OrderItemModel::getQuantity,
                            Integer::sum
                    ));

            // 4. 如果有 SKU，回滚库存
            if (!skuQuantityMap.isEmpty()) {
                rollbackStockForOrder(order);
                log.info("库存回滚成功: orderNo={}, skuCount={}", orderNo, skuQuantityMap.size());
            }

            // 5. 更新订单状态为已取消
            updateOrderStatus(orderNo, OrderStatus.CANCELLED);

            // 6. 可选：发送订单取消通知
            // sendOrderCancelledNotification(order);

            log.info("超时订单取消成功: orderNo={}", orderNo);

        } catch (Exception e) {
            log.error("取消超时订单失败: orderNo={}", orderNo, e);
           // throw new BusinessException("取消订单失败: " + e.getMessage());
        }
    }

    // ==================== 回滚订单库存 ====================

    @Transactional(rollbackFor = Exception.class)
    public void rollbackStockForOrder(OrderModel order) {
        String orderNo = order.getOrderNo();
        log.info("开始回滚订单库存: orderNo={}", orderNo);

        // 1. 查询订单明细
        List<OrderItemModel> orderItems = orderItemMapper.selectByOrderNo(orderNo);
        if (orderItems == null || orderItems.isEmpty()) {
            log.warn("订单明细为空: orderNo={}", orderNo);
            return;
        }

        // 2. 按 SKU 汇总数量
        Map<String, Integer> skuQuantityMap = orderItems.stream()
                .filter(item -> item.getSkuId() != null && !item.getSkuId().isEmpty())
                .collect(Collectors.toMap(
                        OrderItemModel::getSkuId,
                        OrderItemModel::getQuantity,
                        Integer::sum
                ));

        if (skuQuantityMap.isEmpty()) {
            log.warn("订单中没有SKU商品: orderNo={}", orderNo);
            return;
        }

        // 3. 查询 SKU 最新数据
        List<String> skuIds = new ArrayList<>(skuQuantityMap.keySet());
        List<SkuModel> skuList = skuMapper.selectBatchIds(skuIds);

        // 4. 构建批量回滚参数（增加 locked_stock 和 available_stock）
        List<SkuStockUpdate> updateList = new ArrayList<>();
        for (SkuModel sku : skuList) {
            Integer quantity = skuQuantityMap.get(sku.getId());
            if (quantity == null || quantity <= 0) {
                continue;
            }

            // 检查锁定库存是否足够回滚
            if (sku.getLockedStock() == null || sku.getLockedStock() < quantity) {
                log.warn("锁定库存不足，无法回滚: orderNo={}, skuId={}, locked={}, need={}",
                        orderNo, sku.getId(), sku.getLockedStock(), quantity);
                // 继续执行，不抛异常，避免订单无法取消
                continue;
            }

            updateList.add(new SkuStockUpdate(
                    sku.getId(),
                    quantity,
                    sku.getVersion()
            ));
        }

        if (updateList.isEmpty()) {
            log.warn("没有需要回滚库存的SKU: orderNo={}", orderNo);
            return;
        }

        // 5. 批量回滚库存（释放锁定库存，恢复可用库存）
        int affectedRows = skuMapper.batchRollbackStock(updateList);

        if (affectedRows != updateList.size()) {
            log.warn("批量回滚库存部分失败: orderNo={}, 预期{}个, 实际{}个",
                    orderNo, updateList.size(), affectedRows);
            // 不抛异常，避免订单无法取消
        }

        log.info("订单库存回滚成功: orderNo={}, 回滚{}个SKU", orderNo, affectedRows);
    }

    // ==================== 更新订单状态 ====================

    public void updateOrderStatus(String orderNo, OrderStatus status) {
        OrderModel updateOrder = new OrderModel();
        updateOrder.setOrderNo(orderNo);
        updateOrder.setStatus(status.name());
//        updateOrder.setCancelTime(LocalDateTime.now());
        updateOrder.setUpdatedAt(LocalDateTime.now());

        int affected = orderMapper.updateByOrderNo(updateOrder);
        if (affected == 0) {
            log.warn("更新订单状态失败: orderNo={}, status={}", orderNo, status.name());
        }
    }

    // ==================== 其他方法 ====================

    public OrderModel getById(String orderId) {
        return orderMapper.selectById(orderId);
    }

}
