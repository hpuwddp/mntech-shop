package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.AddressRequest;
import com.mntech.shop.order.dto.response.AddressResponse;
import com.mntech.shop.order.model.UserAddress;

import java.util.List;

public interface IAddressService {
    /**
     * 添加地址
     */
    AddressResponse addAddress(String userId, AddressRequest request);

    /**
     * 更新地址
     */
    AddressResponse updateAddress(String userId, String addressId, AddressRequest request);

    /**
     * 删除地址
     */
    void deleteAddress(String userId, String addressId);

    /**
     * 设置默认地址
     */
    void setDefaultAddress(String userId, String addressId);

    /**
     * 查询用户所有地址
     */
    List<AddressResponse> getAddresses(String userId);

    /**
     * 查询用户默认地址
     */
    AddressResponse getDefaultAddress(String userId);

    /**
     * 查询地址详情
     */
    AddressResponse getAddressDetail(String userId, String addressId);
}
