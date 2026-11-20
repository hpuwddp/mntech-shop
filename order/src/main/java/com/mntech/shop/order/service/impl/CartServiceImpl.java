package com.mntech.shop.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.dao.CartMapper;
import com.mntech.shop.order.dto.request.CartAddRequest;
import com.mntech.shop.order.dto.response.CartResponse;
import com.mntech.shop.order.model.CartModel;
import com.mntech.shop.order.service.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse addToCart(CartAddRequest request) {
        // 1. 查询是否已存在
        CartModel exist = cartMapper.selectByUserAndProduct(
                request.getUserId(),
                request.getProductId(),
                request.getSkuId()
        );

        if (exist != null) {
            // 已存在，增加数量
            int newQuantity = exist.getQuantity() + request.getQuantity();
            if (newQuantity > request.getStock()) {
                throw new BusinessException("库存不足，当前库存：" + request.getStock());
            }
            exist.setQuantity(newQuantity);
            exist.setUpdatedAt(LocalDateTime.now());
            cartMapper.updateById(exist);
            log.info("购物车商品数量更新: userId={}, productId={}, quantity={}",
                    request.getUserId(), request.getProductId(), newQuantity);
        } else {
            // 不存在，新增
            CartModel cart = new CartModel();
            BeanUtil.copyProperties(request, cart);
            cart.setPrice(BigDecimal.valueOf(request.getPrice()));
            cart.setSelected(true);
            cart.setCreatedAt(LocalDateTime.now());
            cart.setUpdatedAt(LocalDateTime.now());
            cart.setDeleted(0);
            cartMapper.insert(cart);
            log.info("商品加入购物车: userId={}, productId={}", request.getUserId(), request.getProductId());
        }

        return getCart(request.getUserId());
    }

    @Override
    public CartResponse getCart(String userId) {
        List<CartModel> cartList = cartMapper.selectByUserId(userId);
        return buildCartResponse(cartList);
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(String userId, String productId, String skuId, Integer quantity) {
        CartModel cart = cartMapper.selectByUserAndProduct(userId, productId, skuId);
        if (cart == null) {
            throw new BusinessException("购物车商品不存在");
        }

        if (quantity <= 0) {
            // 数量为0，删除
            cartMapper.deleteById(cart.getId());
        } else {
            if (quantity > cart.getStock()) {
                throw new BusinessException("库存不足，当前库存：" + cart.getStock());
            }
            cart.setQuantity(quantity);
            cart.setUpdatedAt(LocalDateTime.now());
            cartMapper.updateById(cart);
        }

        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResponse updateSelected(String userId, String productId, String skuId, Boolean selected) {
        CartModel cart = cartMapper.selectByUserAndProduct(userId, productId, skuId);
        if (cart == null) {
            throw new BusinessException("购物车商品不存在");
        }

        cart.setSelected(selected);
        cart.setUpdatedAt(LocalDateTime.now());
        cartMapper.updateById(cart);

        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResponse toggleSelectAll(String userId, Boolean selected) {
        List<CartModel> cartList = cartMapper.selectByUserId(userId);
        if (CollectionUtil.isNotEmpty(cartList)) {
            for (CartModel cart : cartList) {
                cart.setSelected(selected);
                cart.setUpdatedAt(LocalDateTime.now());
                cartMapper.updateById(cart);
            }
        }
        return getCart(userId);
    }

    @Override
    @Transactional
    public CartResponse deleteItem(String userId, String productId, String skuId) {
        CartModel cart = cartMapper.selectByUserAndProduct(userId, productId, skuId);
        if (cart != null) {
            cartMapper.deleteById(cart.getId());
        }
        return getCart(userId);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        cartMapper.clearCart(userId);
    }

    @Override
    public CartResponse checkout(String userId) {
        List<CartModel> cartList = cartMapper.selectSelectedByUserId(userId);
        return buildCartResponse(cartList);
    }

    // ===== 私有方法 =====

    private CartResponse buildCartResponse(List<CartModel> cartList) {
        if (CollectionUtil.isEmpty(cartList)) {
            return CartResponse.builder()
                    .items(new ArrayList<>())
                    .totalCount(0)
                    .selectedCount(0)
                    .selectedTotalAmount(BigDecimal.ZERO)
                    .build();
        }

        List<CartResponse.CartItemResponse> items = cartList.stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());

        int totalCount = cartList.stream().mapToInt(CartModel::getQuantity).sum();
        int selectedCount = cartList.stream()
                .filter(CartModel::getSelected)
                .mapToInt(CartModel::getQuantity)
                .sum();
        BigDecimal selectedTotalAmount = cartList.stream()
                .filter(CartModel::getSelected)
                .map(c -> c.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .items(items)
                .totalCount(totalCount)
                .selectedCount(selectedCount)
                .selectedTotalAmount(selectedTotalAmount)
                .build();
    }

    private CartResponse.CartItemResponse convertToItemResponse(CartModel cart) {
        return CartResponse.CartItemResponse.builder()
                .id(cart.getId())
                .productId(cart.getProductId())
                .skuId(cart.getSkuId())
                .productName(cart.getProductName())
                .skuName(cart.getSkuName())
                .productImage(cart.getProductImage())
                .price(cart.getPrice())
                .quantity(cart.getQuantity())
                .stock(cart.getStock())
                .selected(cart.getSelected())
                .totalPrice(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
                .build();
    }
}
