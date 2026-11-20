package com.mntech.shop.order.service;

/**
 * 平台配置读取服务（key-value）
 */
public interface IShopConfigService {

    /**
     * 读取配置值；不存在或为空返回 null
     */
    String getValue(String key);

    /**
     * 读取配置值；不存在或为空返回默认值
     */
    String getString(String key, String defaultValue);

    /**
     * 检查微信小程序 appid/secret 是否已配置（后台或 yml 任一来源）
     */
    boolean isWechatConfigured();
}