package com.mntech.shop.order.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mntech.shop.order.dto.response.WechatSessionResponse;
import com.mntech.shop.order.service.IShopConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class WechatUtil {

    private final IShopConfigService shopConfigService;

    @Value("${loginwechat.appid:}")
    private String ymlAppid;

    @Value("${loginwechat.secret:}")
    private String ymlSecret;

    private static final String SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token";
    private static final String WXACODE_URL = "https://api.weixin.qq.com/wxa/getwxacodeunlimit";

    /** access_token 内存缓存（微信有效期 7200 秒，提前 200 秒刷新） */
    private volatile String cachedAccessToken;
    private volatile long cachedAccessTokenExpireAt;

    public WechatUtil(IShopConfigService shopConfigService) {
        this.shopConfigService = shopConfigService;
    }

    /**
     * appid 解析：后台 shop_config.wechat.appid 优先，回退 application.yml
     */
    private String resolveAppid() {
        return StrUtil.blankToDefault(shopConfigService.getValue("wechat.appid"), ymlAppid);
    }

    /**
     * secret 解析：后台 shop_config.wechat.secret 优先，回退 application.yml
     */
    private String resolveSecret() {
        return StrUtil.blankToDefault(shopConfigService.getValue("wechat.secret"), ymlSecret);
    }

    /**
     * 获取微信 session
     */
    public WechatSessionResponse getSession(String code) {
        try {
            String url = SESSION_URL + "?appid=" + resolveAppid() + "&secret=" + resolveSecret() + "&js_code=" + code + "&grant_type=authorization_code";
            String result = HttpRequest.get(url).timeout(10000).execute().body();
            log.info("微信登录返回: {}", result);

            JSONObject json = JSONUtil.parseObj(result);
            WechatSessionResponse response = new WechatSessionResponse();
            response.setOpenid(json.getStr("openid"));
            response.setSession_key(json.getStr("session_key"));
            response.setUnionid(json.getStr("unionid"));
            response.setErrcode(json.getInt("errcode", 0));
            response.setErrmsg(json.getStr("errmsg"));

            if (response.getErrcode() != 0) {
                log.error("微信登录失败: errcode={}, errmsg={}", response.getErrcode(), response.getErrmsg());
            }

            return response;
        } catch (Exception e) {
            log.error("获取微信session失败", e);
            WechatSessionResponse response = new WechatSessionResponse();
            response.setErrcode(-1);
            response.setErrmsg("网络异常");
            return response;
        }
    }

    /**
     * 获取微信全局 access_token（带内存缓存）
     */
    public String getAccessToken() {
        if (cachedAccessToken != null && System.currentTimeMillis() < cachedAccessTokenExpireAt) {
            return cachedAccessToken;
        }
        String appid = resolveAppid();
        String secret = resolveSecret();
        if (StrUtil.isBlank(appid) || StrUtil.isBlank(secret)) {
            log.warn("未配置微信小程序 appid/secret，无法获取 access_token");
            return null;
        }
        try {
            String url = TOKEN_URL + "?grant_type=client_credential&appid=" + appid + "&secret=" + secret;
            String body = HttpRequest.get(url).timeout(10000).execute().body();
            JSONObject json = JSONUtil.parseObj(body);
            String token = json.getStr("access_token");
            if (StrUtil.isBlank(token)) {
                log.warn("获取微信 access_token 失败: errcode={}, errmsg={}", json.getInt("errcode"), json.getStr("errmsg"));
                return null;
            }
            int expiresIn = json.getInt("expires_in", 7200);
            cachedAccessToken = token;
            cachedAccessTokenExpireAt = System.currentTimeMillis() + (expiresIn - 200L) * 1000;
            log.info("获取微信 access_token 成功，{} 秒后过期", expiresIn);
            return token;
        } catch (Exception e) {
            log.error("获取微信 access_token 异常", e);
            return null;
        }
    }

    /**
     * 生成小程序码（getwxacodeunlimit）。
     * scene 限制 32 字符，业务上直接传用户 userId。
     * 成功返回 PNG 字节；失败/未配置返回 null（调用方回退普通二维码）。
     */
    public byte[] getWxacodeUnlimit(String scene, String page, int width) {
        if (StrUtil.isBlank(scene) || scene.length() > 32) {
            log.warn("生成小程序码失败: scene 不能为空且不能超过 32 字符, scene={}", scene);
            return null;
        }
        String accessToken = getAccessToken();
        if (StrUtil.isBlank(accessToken)) {
            return null;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("scene", scene);
            body.put("page", page);
            body.put("width", width);
            body.put("check_path", false);
            body.put("env_version", "release");
            HttpResponse resp = HttpRequest.post(WXACODE_URL + "?access_token=" + accessToken)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(body))
                    .timeout(20000)
                    .execute();
            byte[] bytes = resp.bodyBytes();
            if (bytes == null || bytes.length == 0) {
                log.warn("生成小程序码失败: 响应为空, scene={}", scene);
                return null;
            }
            String contentType = resp.header("Content-Type");
            if (contentType != null && contentType.toLowerCase().contains("image")) {
                log.info("生成小程序码成功: scene={}, page={}, bytes={}", scene, page, bytes.length);
                return bytes;
            }
            // 非图片即错误 JSON
            try {
                String errText = new String(bytes, StandardCharsets.UTF_8);
                JSONObject err = JSONUtil.parseObj(errText);
                log.warn("生成小程序码失败: errcode={}, errmsg={}, scene={}", err.getInt("errcode"), err.getStr("errmsg"), scene);
            } catch (Exception ignore) {
                log.warn("生成小程序码失败: 非图片响应 content-type={}, len={}", contentType, bytes.length);
            }
            return null;
        } catch (Exception e) {
            log.error("生成小程序码异常: scene={}, page={}", scene, page, e);
            return null;
        }
    }
}