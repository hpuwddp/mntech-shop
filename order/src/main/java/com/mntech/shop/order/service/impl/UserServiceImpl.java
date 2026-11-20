package com.mntech.shop.order.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.config.JwtUtil;
import com.mntech.shop.order.config.WechatUtil;
import com.mntech.shop.order.dao.UserMapper;
import com.mntech.shop.order.dto.request.UpdateGenderRequest;
import com.mntech.shop.order.dto.request.UpdateNicknameRequest;
import com.mntech.shop.order.dto.request.UpdateProfileRequest;
import com.mntech.shop.order.dto.request.WechatLoginRequest;
import com.mntech.shop.order.dto.response.UserProfileResponse;
import com.mntech.shop.order.dto.response.WechatLoginResponse;
import com.mntech.shop.order.dto.response.WechatSessionResponse;
import com.mntech.shop.order.model.UserModel;
import com.mntech.shop.order.service.IUserService;
import com.mntech.shop.order.util.RandomNicknameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;
    private final WechatUtil wechatUtil;
    private final JwtUtil jwtUtil;

//    @Value("${jwt.expire}")
//    private Long jwtExpire;

    @Override
    @Transactional
    public WechatLoginResponse wechatLogin(WechatLoginRequest request) {
        // 1. 获取微信session
        WechatSessionResponse session = wechatUtil.getSession(request.getCode());
        if (session.getErrcode() != 0) {
            throw new BusinessException("微信登录失败: " + session.getErrmsg());
        }

        String openid = session.getOpenid();
        if (StrUtil.isBlank(openid)) {
            throw new BusinessException("获取用户信息失败");
        }

        // 2. 查询用户是否存在
        UserModel user = userMapper.selectByOpenid(openid);
        boolean isNewUser = false;
        if (user == null) {
            // 新用户，创建
            user = new UserModel();
            user.setOpenid(openid);
            user.setUnionid(session.getUnionid());
            user.setSessionKey(session.getSession_key());
            user.setStatus("ACTIVE");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            user.setDeleted(0);
            user.setNickname(RandomNicknameGenerator.generate());
            userMapper.insert(user);
            isNewUser = true;
            log.info("新用户注册: openid={}", openid);
        } else {
            // 老用户，更新session_key
            user.setSessionKey(session.getSession_key());
            if (StrUtil.isNotBlank(session.getUnionid())) {
                user.setUnionid(session.getUnionid());
            }
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
            log.info("用户登录: openid={}", openid);
        }

        // 3. 生成JWT token
        String token = jwtUtil.generateToken(user.getId(),user.getNickname());

        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(3600000L);
        userMapper.updateToken(user.getId(), token, expireTime);

        user.setToken(token);

        // 5. 返回登录结果
        return WechatLoginResponse.builder()
                .userId(user.getId())
                .token(token)
                .expireTime(3600000L)
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .isNewUser(isNewUser)
                .build();
    }

    @Override
    public void logout(String userId) {
        userMapper.clearToken(userId);
        log.info("用户退出登录: userId={}", userId);
    }

//    @Override
//    public boolean validateToken(String token) {
//        if (StrUtil.isBlank(token)) {
//            return false;
//        }
//
//        if (!jwtUtil.validateToken(token)) {
//            return false;
//        }
//
//        String userId = jwtUtil.getUserIdFromToken(token);
//        if (StrUtil.isBlank(userId)) {
//            return false;
//        }
//
//        UserModel user = userMapper.selectByToken(token);
//        if (user == null) {
//            return false;
//        }
//
//        if (user.getTokenExpireTime() != null && user.getTokenExpireTime().isBefore(LocalDateTime.now())) {
//            return false;
//        }
//
//        return true;
//    }
//
//    @Override
//    public String getCurrentUserId(String token) {
//        if (StrUtil.isBlank(token)) {
//            return null;
//        }
//        return jwtUtil.getUserIdFromToken(token);
//    }

    @Override
    public UserProfileResponse getProfile(String userId) {
        UserModel user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateNickname(String userId, UpdateNicknameRequest request) {
        UserModel user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String nickname = request.getNickname().trim();
        if (nickname.length() > 20) {
            throw new BusinessException("昵称不能超过20个字符");
        }

        int result = userMapper.updateNickname(userId, nickname);
        if (result == 0) {
            throw new BusinessException("更新昵称失败");
        }

        log.info("用户 {} 更新昵称为: {}", userId, nickname);
        return getProfile(userId);
    }

    @Override
    @Transactional
    public UserProfileResponse updateGender(String userId, UpdateGenderRequest request) {
        UserModel user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String gender = request.getGender();
        if (!"MALE".equals(gender) && !"FEMALE".equals(gender) && !"SECRET".equals(gender)) {
            throw new BusinessException("性别参数错误: 请使用 MALE, FEMALE, SECRET");
        }

        int result = userMapper.updateGender(userId, gender);
        if (result == 0) {
            throw new BusinessException("更新性别失败");
        }

        log.info("用户 {} 更新性别为: {}", userId, gender);
        return getProfile(userId);
    }

    @Override
    @Transactional
    public UserProfileResponse updateAvatar(String userId, String avatarUrl) {
        UserModel user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (StrUtil.isBlank(avatarUrl)) {
            throw new BusinessException("头像地址不能为空");
        }

        int result = userMapper.updateAvatar(userId, avatarUrl);
        if (result == 0) {
            throw new BusinessException("更新头像失败");
        }

        log.info("用户 {} 更新头像为: {}", userId, avatarUrl);
        return getProfile(userId);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        UserModel user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String nickname = request.getNickname();
        String gender = request.getGender();

        // 校验昵称
        if (StrUtil.isNotBlank(nickname)) {
            nickname = nickname.trim();
            if (nickname.length() > 20) {
                throw new BusinessException("昵称不能超过20个字符");
            }
        }

        // 校验性别
        if (StrUtil.isNotBlank(gender)) {
            if (!"MALE".equals(gender) && !"FEMALE".equals(gender) && !"SECRET".equals(gender)) {
                throw new BusinessException("性别参数错误");
            }
        }

        int result = userMapper.updateProfile(userId, nickname, gender);
        if (result == 0) {
            throw new BusinessException("更新个人信息失败");
        }

        log.info("用户 {} 更新个人信息: nickname={}, gender={}", userId, nickname, gender);
        return getProfile(userId);
    }

    // ===== 私有方法 =====

    private UserProfileResponse convertToProfileResponse(UserModel user) {
        String createTime = user.getCreatedAt() != null ?
                user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null;

        return UserProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .gender(user.getGender() != null ? user.getGender() : 0)
                .createTime(createTime)
                .build();
    }
}