package com.mntech.shop.order.controller;

import com.mntech.shop.order.dto.request.CartAddRequest;
import com.mntech.shop.order.dto.response.CartResponse;
import com.mntech.shop.order.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final ICartService cartService;

    /**
     * 获取购物车
     */
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable String userId) {
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody CartAddRequest request) {
        CartResponse response = cartService.addToCart(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/{userId}/{productId}/{skuId}/quantity")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable String userId, @PathVariable String productId, @PathVariable String skuId, @RequestParam Integer quantity) {
        CartResponse response = cartService.updateQuantity(userId, productId, skuId, quantity);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新购物车商品选中状态
     */
    @PutMapping("/{userId}/{productId}/{skuId}/selected")
    public ResponseEntity<CartResponse> updateSelected(@PathVariable String userId, @PathVariable String productId, @PathVariable String skuId, @RequestParam Boolean selected) {
        CartResponse response = cartService.updateSelected(userId, productId, skuId, selected);
        return ResponseEntity.ok(response);
    }

    /**
     * 全选/取消全选
     */
    @PutMapping("/{userId}/select-all")
    public ResponseEntity<CartResponse> toggleSelectAll(@PathVariable String userId, @RequestParam Boolean selected) {
        CartResponse response = cartService.toggleSelectAll(userId, selected);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/{userId}/{productId}/{skuId}")
    public ResponseEntity<CartResponse> deleteItem(@PathVariable String userId, @PathVariable String productId, @PathVariable String skuId) {
        CartResponse response = cartService.deleteItem(userId, productId, skuId);
        return ResponseEntity.ok(response);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 结算（获取选中的购物车项）
     */
    @GetMapping("/{userId}/checkout")
    public ResponseEntity<CartResponse> checkout(@PathVariable String userId) {
        CartResponse response = cartService.checkout(userId);
        return ResponseEntity.ok(response);
    }
}
