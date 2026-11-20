package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.CartAddRequest;
import com.mntech.shop.order.dto.response.CartResponse;

public interface ICartService {
    /**
     * 添加商品到购物车
     */
    CartResponse addToCart(CartAddRequest request);

    /**
     * 获取用户购物车
     */
    CartResponse getCart(String userId);

    /**
     * 更新购物车商品数量
     */
    CartResponse updateQuantity(String userId, String productId, String skuId, Integer quantity);

    /**
     * 更新购物车选中状态
     */
    CartResponse updateSelected(String userId, String productId, String skuId, Boolean selected);

    /**
     * 全选/取消全选
     */
    CartResponse toggleSelectAll(String userId, Boolean selected);

    /**
     * 删除购物车商品
     */
    CartResponse deleteItem(String userId, String productId, String skuId);

    /**
     * 清空购物车
     */
    void clearCart(String userId);

    /**
     * 结算（获取选中的购物车项）
     */
    CartResponse checkout(String userId);
}
