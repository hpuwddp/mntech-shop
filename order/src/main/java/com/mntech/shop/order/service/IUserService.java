package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.UpdateGenderRequest;
import com.mntech.shop.order.dto.request.UpdateNicknameRequest;
import com.mntech.shop.order.dto.request.UpdateProfileRequest;
import com.mntech.shop.order.dto.request.WechatLoginRequest;
import com.mntech.shop.order.dto.response.UserProfileResponse;
import com.mntech.shop.order.dto.response.WechatLoginResponse;

public interface IUserService {
    WechatLoginResponse wechatLogin(WechatLoginRequest request);

    void logout(String userId);

//    boolean validateToken(String token);
//
//    String getCurrentUserId(String token);

    /**
     * 获取用户个人信息
     */
    UserProfileResponse getProfile(String userId);

    /**
     * 更新昵称
     */
    UserProfileResponse updateNickname(String userId, UpdateNicknameRequest request);

    /**
     * 更新性别
     */
    UserProfileResponse updateGender(String userId, UpdateGenderRequest request);

    /**
     * 更新头像
     */
    UserProfileResponse updateAvatar(String userId, String avatarUrl);

    /**
     * 更新个人信息（昵称+性别）
     */
    UserProfileResponse updateProfile(String userId, UpdateProfileRequest request);
}
