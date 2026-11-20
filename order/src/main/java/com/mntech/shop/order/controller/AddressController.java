package com.mntech.shop.order.controller;

import com.mntech.shop.order.dto.request.AddressRequest;
import com.mntech.shop.order.dto.response.AddressResponse;
import com.mntech.shop.order.service.IAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
@Validated
public class AddressController {
    private final IAddressService addressService;

    /**
     * 获取用户所有地址
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<AddressResponse>> getAddresses(@PathVariable String userId) {
        List<AddressResponse> addresses = addressService.getAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    /**
     * 获取用户默认地址
     */
    @GetMapping("/{userId}/default")
    public ResponseEntity<AddressResponse> getDefaultAddress(@PathVariable String userId) {
        AddressResponse address = addressService.getDefaultAddress(userId);
        if (address == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(address);
    }

    /**
     * 获取地址详情
     */
    @GetMapping("/{userId}/{addressId}")
    public ResponseEntity<AddressResponse> getAddressDetail(@PathVariable String userId, @PathVariable String addressId) {
        AddressResponse address = addressService.getAddressDetail(userId, addressId);
        return ResponseEntity.ok(address);
    }

    /**
     * 添加地址
     */
    @PostMapping("/{userId}")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable String userId,
                                                      @Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.addAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    /**
     * 更新地址
     */
    @PutMapping("/{userId}/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable String userId, @PathVariable String addressId,
                                                         @Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(address);
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{userId}/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable String userId, @PathVariable String addressId) {
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 设置默认地址
     */
    @PostMapping("/{userId}/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(@PathVariable String userId, @PathVariable String addressId) {
        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.ok().build();
    }
}
