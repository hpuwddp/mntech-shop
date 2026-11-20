package com.mntech.shop.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mntech.shop.order.dao.ShopConfigMapper;
import com.mntech.shop.order.model.ShopConfigModel;
import com.mntech.shop.order.service.IShopConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 平台配置读取服务：优先读 shop_config 表（后台管理系统可配置），
 * 微信 appid/secret 未配置时回退 application.yml 的 loginwechat.*
 */
@Service
@RequiredArgsConstructor
public class ShopConfigServiceImpl implements IShopConfigService {

    private final ShopConfigMapper shopConfigMapper;

    @Value("${loginwechat.appid:}")
    private String loginAppid;

    @Value("${loginwechat.secret:}")
    private String loginSecret;

    @Override
    public String getValue(String key) {
        if (StrUtil.isBlank(key)) {
            return null;
        }
        ShopConfigModel cfg = shopConfigMapper.selectOne(
                new LambdaQueryWrapper<ShopConfigModel>()
                        .eq(ShopConfigModel::getConfigKey, key)
                        .last("limit 1"));
        return cfg != null && StrUtil.isNotBlank(cfg.getConfigValue()) ? cfg.getConfigValue() : null;
    }

    @Override
    public String getString(String key, String defaultValue) {
        String value = getValue(key);
        return StrUtil.isBlank(value) ? defaultValue : value;
    }

    @Override
    public boolean isWechatConfigured() {
        return StrUtil.isNotBlank(getValue("wechat.appid"))
                && StrUtil.isNotBlank(getValue("wechat.secret"));
    }

    /**
     * 供 WechatUtil 使用的 appid 解析：shop_config 优先，回退 yml
     */
    public String resolveAppid() {
        String cfg = getValue("wechat.appid");
        return StrUtil.isNotBlank(cfg) ? cfg : loginAppid;
    }

    /**
     * 供 WechatUtil 使用的 secret 解析：shop_config 优先，回退 yml
     */
    public String resolveSecret() {
        String cfg = getValue("wechat.secret");
        return StrUtil.isNotBlank(cfg) ? cfg : loginSecret;
    }
}