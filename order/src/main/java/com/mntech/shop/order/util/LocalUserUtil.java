package com.mntech.shop.order.util;

import cn.hutool.core.util.StrUtil;

import javax.servlet.http.HttpServletRequest;

public class LocalUserUtil {
    // 获取网关传递过来的用户ID
    public static String getCurrentUserId(HttpServletRequest request){
        String userIdStr = request.getHeader("X-User-Id");
        if(StrUtil.isBlank(userIdStr)){
            return null;
        }
        return userIdStr;
    }
}
