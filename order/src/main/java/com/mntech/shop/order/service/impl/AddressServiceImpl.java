package com.mntech.shop.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.dao.UserAddressMapper;
import com.mntech.shop.order.dto.request.AddressRequest;
import com.mntech.shop.order.dto.response.AddressResponse;
import com.mntech.shop.order.model.UserAddress;
import com.mntech.shop.order.service.IAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AddressServiceImpl implements IAddressService {

    private final UserAddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponse addAddress(String userId, AddressRequest request) {
        // 1. 检查地址数量限制（最多20个）
        int count = addressMapper.countByUserId(userId);
        if (count >= 20) {
            throw new BusinessException("地址数量已达上限（20个）");
        }

        // 2. 如果设置为默认地址，取消其他默认地址
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressMapper.cancelDefault(userId);
        }

        // 3. 如果是第一个地址，自动设为默认
        if (count == 0) {
            request.setIsDefault(true);
        }

        // 4. 构建实体
        UserAddress address = new UserAddress();
        BeanUtil.copyProperties(request, address);
        address.setUserId(userId);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        address.setDeleted(0);

        addressMapper.insert(address);
        log.info("用户 {} 添加地址成功: {}", userId, address.getId());

        return convertToResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String userId, String addressId, AddressRequest request) {
        // 1. 查询地址
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            throw new BusinessException("无权操作该地址");
        }

        // 2. 如果设置为默认地址，取消其他默认地址
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressMapper.cancelDefault(userId);
        }

        // 3. 更新字段
        if (request.getReceiverName() != null) {
            address.setReceiverName(request.getReceiverName());
        }
        if (request.getReceiverPhone() != null) {
            address.setReceiverPhone(request.getReceiverPhone());
        }
        if (request.getProvince() != null) {
            address.setProvince(request.getProvince());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getDistrict() != null) {
            address.setDistrict(request.getDistrict());
        }
        if (request.getDetailAddress() != null) {
            address.setDetailAddress(request.getDetailAddress());
        }
        if (request.getPostalCode() != null) {
            address.setPostalCode(request.getPostalCode());
        }
        if (request.getTag() != null) {
            address.setTag(request.getTag());
        }
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(address);

        log.info("用户 {} 更新地址成功: {}", userId, addressId);
        return convertToResponse(address);
    }

    @Override
    @Transactional
    public void deleteAddress(String userId, String addressId) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            throw new BusinessException("无权操作该地址");
        }

        // 如果删除的是默认地址，需要重新设置一个默认地址
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.deleteById(addressId);
            // 查找其他地址设为默认
            List<UserAddress> addresses = addressMapper.selectByUserId(userId);
            if (CollectionUtil.isNotEmpty(addresses)) {
                UserAddress first = addresses.get(0);
                first.setIsDefault(true);
                first.setUpdatedAt(LocalDateTime.now());
                addressMapper.updateById(first);
            }
        } else {
            addressMapper.deleteById(addressId);
        }

        log.info("用户 {} 删除地址成功: {}", userId, addressId);
    }

    @Override
    @Transactional
    public void setDefaultAddress(String userId, String addressId) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            throw new BusinessException("无权操作该地址");
        }

        // 取消所有默认
        addressMapper.cancelDefault(userId);

        // 设置当前为默认
        address.setIsDefault(true);
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(address);

        log.info("用户 {} 设置默认地址成功: {}", userId, addressId);
    }

    @Override
    public List<AddressResponse> getAddresses(String userId) {
        List<UserAddress> addresses = addressMapper.selectByUserId(userId);
        List<AddressResponse> result = new ArrayList<>();
        for (UserAddress address : addresses) {
            result.add(convertToResponse(address));
        }
        return result;
    }

    @Override
    public AddressResponse getDefaultAddress(String userId) {
        UserAddress address = addressMapper.selectDefaultByUserId(userId);
        if (address == null) {
            return null;
        }
        return convertToResponse(address);
    }

    @Override
    public AddressResponse getAddressDetail(String userId, String addressId) {
        UserAddress address = addressMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            throw new BusinessException("无权查看该地址");
        }
        return convertToResponse(address);
    }

    // ===== 私有方法 =====

    private AddressResponse convertToResponse(UserAddress address) {
        AddressResponse response = new AddressResponse();
        BeanUtil.copyProperties(address, response);

        // 组装完整地址
        StringBuilder fullAddress = new StringBuilder();
        if (address.getProvince() != null) {
            fullAddress.append(address.getProvince());
        }
        if (address.getCity() != null) {
            fullAddress.append(address.getCity());
        }
        if (address.getDistrict() != null) {
            fullAddress.append(address.getDistrict());
        }
        if (address.getDetailAddress() != null) {
            fullAddress.append(address.getDetailAddress());
        }
        response.setFullAddress(fullAddress.toString());

        return response;
    }
}
