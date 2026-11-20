package com.mntech.shop.order.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.model.v2.Result;
import com.mntech.shop.order.dto.request.UpdateGenderRequest;
import com.mntech.shop.order.dto.request.UpdateNicknameRequest;
import com.mntech.shop.order.dto.request.UpdateProfileRequest;
import com.mntech.shop.order.dto.request.WechatLoginRequest;
import com.mntech.shop.order.dto.response.UserProfileResponse;
import com.mntech.shop.order.dto.response.WechatLoginResponse;
import com.mntech.shop.order.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final IUserService userService;

    /**
     * 微信登录
     */
    @PostMapping("/login/wechat")
    public ResponseEntity<WechatLoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        WechatLoginResponse response = userService.wechatLogin(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("x-access-token") String token) {
//        String userId = userService.getCurrentUserId(token);
//        if (userId != null) {
//            userService.logout(userId);
//        }
        System.out.println("logout"+token);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "退出成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 验证Token
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("x-access-token") String token) {
//        boolean valid = userService.validateToken(token);
        System.out.println("validateToken---"+token);
        Map<String, Object> result = new HashMap<>();
//        result.put("valid", valid);
//        result.put("message", valid ? "Token有效" : "Token无效");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable String userId) {
        UserProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/nickname/{userId}")
    public ResponseEntity<UserProfileResponse> updateNickname(@PathVariable String userId,
                                                              @Valid @RequestBody UpdateNicknameRequest request) {
        UserProfileResponse response = userService.updateNickname(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/gender/{userId}")
    public ResponseEntity<UserProfileResponse> updateGender(@PathVariable String userId,
                                                            @Valid @RequestBody UpdateGenderRequest request) {
        UserProfileResponse response = userService.updateGender(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/avatar/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileResponse> updateAvatar(@PathVariable String userId,
                                                            @RequestParam("file") MultipartFile file) {
        // 上传文件获取URL（实际项目中实现）
        String avatarUrl = uploadAvatar(file);
        UserProfileResponse response = userService.updateAvatar(userId, avatarUrl);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<UserProfileResponse> updateProfile(@PathVariable String userId,
                                                             @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 上传头像（实际项目中实现）
     * 目前项目中并未实现
     */
    private String uploadAvatar(MultipartFile file) {
        // TODO: 实现文件上传到OSS或本地
        return "https://cdn.example.com/avatar/xxx.jpg";
    }
}
