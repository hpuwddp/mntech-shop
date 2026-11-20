package com.mntech.shop.order.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.HttpException;
import com.wechat.pay.java.core.exception.MalformedMessageException;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.QueryByOutRefundNoRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.system.ApplicationHome;

import java.security.PrivateKey;
import java.util.Objects;

public class WxPayUtil {
    // appID
    private static String appid = "xxx";
    // 商户号
    private static String merchantId = "xxx";
    // 商户证书序列号
    private static String merchantSerialNumber = "xxx";
    // 商户APIV3密钥
    private static String apiV3Key = "xxxx";
    // 商户API私钥
    private static String privateKey = "-----BEGIN CERTIFICATE-----\n" +
            "MIIEKDCCAxCgAwIBAgIUOzqxxxxCSqGSIb3DQEBCwUAA4IBAQASYuMY\n" +
            "9erBop0krDxacU5jat4nuh8MECrhSgVW5CQEqa3sfBTjEnudPd/X++lJphbY1IqH\n" +
            "Xrlu7amhFqv4XfNA65AOz5NxxxxBgcJwh2/mu9f9yRojZ061S8WEo\n" +
            "KrCI3riCXg/elYDFHvwpBinngjeGnGXutxxxifnk\n" +
            "wgjALFZiMiXDv/Yo\n" +
            "-----END CERTIFICATE-----\n";
    // 支付回调地址
    private static String payNotifyUrl = "https://www.xxxx.site/api/pay/payCallback";
    // 退款回调地址
    private static String refunNotifyUrl = "https://www.xxxxx.site/api/pay/refunCallback";

    private static JsapiServiceExtension jsapiService;

    private static RefundService refundService;

    public static void initConfig() {
        // 初始化商户配置
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKey(privateKey)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3Key)
                .build();

        // 初始化服务
        jsapiService = new JsapiServiceExtension.Builder().config(config).build();
        refundService = new RefundService.Builder().config(config).build();
    }

    /**
     * @methodName：jsapi
     * @description：JSAPI支付下单
     * @author：tanyp
     * @Params： [orderId, goodsName, openId, amount]
     * @Return： java.lang.String
     * @editNote：
     */
    public static Object jsapi(String orderId, String goodsName, String openId, Integer amount) {
        try {


            if (Objects.isNull(jsapiService)) {
                initConfig();
            }
            PrepayRequest request = new PrepayRequest();
            request.setAppid(appid);
            request.setMchid(merchantId);
            request.setOutTradeNo(orderId);
            request.setDescription(goodsName);
            request.setNotifyUrl(payNotifyUrl);
            // 支付者
            Payer payer = new Payer();
            payer.setOpenid(openId);
            request.setPayer(payer);
            // 订单金额，单位为分（正数）
            Amount _amount = new Amount();
            _amount.setCurrency("CNY");
            _amount.setTotal(amount);
            request.setAmount(_amount);

            //log.info("JSAPI 支付下单请求参数:{}", JSON.toJSON(request));
            PrepayWithRequestPaymentResponse response = jsapiService.prepayWithRequestPayment(request);
            //log.info("JSAPI 支付下单返回参数:{}", JSON.toJSON(response));
            return response;
        } catch (HttpException e) {
            // 发送HTTP请求失败
            //  log.error("支付异常,发送HTTP请求失败：{}", e);
            return e.getMessage();
        } catch (ServiceException e) {
            // log.error("支付异常,服务返回状态异常：{}", e);
            return e.getErrorMessage();
        } catch (MalformedMessageException e) {
            //log.error("支付异常,服务返回成功，返回体类型不合法，或者解析返回体失败：{}", e);
            return e.getMessage();
        }
    }

    /**
     * @methodName：closeOrder
     * @description：关闭订单
     * @author：tanyp
     * @Params： [orderId]
     * @Return： java.lang.String
     * @editNote：
     */
    public static String closeOrder(String orderId) {
        try {
            if (Objects.isNull(jsapiService)) {
                initConfig();
            }
            CloseOrderRequest request = new CloseOrderRequest();
            request.setMchid(merchantId);
            request.setOutTradeNo(orderId);
            jsapiService.closeOrder(request);
            return "ok";
        } catch (HttpException e) {
            // 发送HTTP请求失败
            // log.error("支付异常,发送HTTP请求失败：{}", e);
            return e.getMessage();
        } catch (ServiceException e) {
            //log.error("支付异常,服务返回状态异常：{}", e);
            return e.getErrorMessage();
        } catch (MalformedMessageException e) {
            //  log.error("支付异常,服务返回成功，返回体类型不合法，或者解析返回体失败：{}", e);
            return e.getMessage();
        }
    }

    /**
     * @methodName：queryOrderById
     * @description：微信支付订单号查询订单
     * @author：tanyp
     * @Params： [transactionId]
     * @Return： com.wechat.pay.java.service.payments.model.Transaction
     * @editNote：
     */
    public static Transaction queryOrderById(String transactionId) {
        try {
            if (Objects.isNull(jsapiService)) {
                initConfig();
            }
            QueryOrderByIdRequest request = new QueryOrderByIdRequest();
            request.setMchid(merchantId);
            request.setTransactionId(transactionId);
            return jsapiService.queryOrderById(request);
        } catch (HttpException e) {
            // 发送HTTP请求失败
            //  log.error("支付异常,发送HTTP请求失败：{}", e);
        } catch (ServiceException e) {
            // log.error("支付异常,服务返回状态异常：{}", e);
        } catch (MalformedMessageException e) {
            //  log.error("支付异常,服务返回成功，返回体类型不合法，或者解析返回体失败：{}", e);
        }
        return null;
    }

    /**
     * @methodName：queryOrderByOutTradeNo
     * @description：商户订单号查询订单
     * @author：tanyp
     * @Params： [orderId]
     * @Return： com.wechat.pay.java.service.payments.model.Transaction
     * @editNote：
     */
    public static Transaction queryOrderByOutTradeNo(String orderId) {
        try {
            if (Objects.isNull(jsapiService)) {
                initConfig();
            }
            QueryOrderByOutTradeNoRequest request = new QueryOrderByOutTradeNoRequest();
            request.setMchid(merchantId);
            request.setOutTradeNo(orderId);
            return jsapiService.queryOrderByOutTradeNo(request);
        } catch (HttpException e) {
            // 发送HTTP请求失败
            //  log.error("支付异常,发送HTTP请求失败：{}", e);
        } catch (ServiceException e) {
            // log.error("支付异常,服务返回状态异常：{}", e);
        } catch (MalformedMessageException e) {
            //  log.error("支付异常,服务返回成功，返回体类型不合法，或者解析返回体失败：{}", e);
        }
        return null;
    }

    /**
     * @methodName：refund
     * @description：申请退款
     * @author：tanyp
     * @Params： [transactionId, orderId, refunId, reason, refunAmount, payAmount]
     * @Return： com.wechat.pay.java.service.refund.model.Refund
     * @editNote：
     */
    public static Refund refund(String transactionId, String orderId, String refunId, String reason, Integer refunAmount, Integer payAmount) {
        try {
            if (Objects.isNull(refundService)) {
                initConfig();
            }
            CreateRequest request = new CreateRequest();
            if (Objects.isNull(transactionId)) {
                request.setTransactionId(transactionId);
            }
            request.setOutTradeNo(orderId);
            request.setOutRefundNo(refunId);
            request.setNotifyUrl(refunNotifyUrl);
            request.setReason(reason);
            // 订单金额，单位为分（正数）
            AmountReq _amount = new AmountReq();
            _amount.setCurrency("CNY");
            _amount.setRefund(refunAmount.longValue());
            _amount.setTotal(payAmount.longValue());
            request.setAmount(_amount);

            return refundService.create(request);
        } catch (HttpException e) {
            // 发送HTTP请求失败
            //   log.error("退款异常,发送HTTP请求失败：{}", e);
        } catch (ServiceException e) {
            // log.error("退款异常,服务返回状态异常：{}", e);
        } catch (MalformedMessageException e) {
            // log.error("退款异常,服务返回成功，返回体类型不合法，或者解析返回体失败：{}", e);
        }
        return null;
    }

    /**
     * @methodName：queryByOutRefundNo
     * @description：退款查询
     * @author：tanyp
     * @Params： [refunId]
     * @Return： com.wechat.pay.java.service.refund.model.Refund
     * @editNote：
     */
    public static Refund queryByOutRefundNo(String refunId) {
        try {
            if (Objects.isNull(refundService)) {
                initConfig();
            }
            QueryByOutRefundNoRequest request = new QueryByOutRefundNoRequest();
            request.setOutRefundNo(refunId);
            return refundService.queryByOutRefundNo(request);
        } catch (HttpException e) {
            // 发送HTTP请求失败
            // log.error("退款异常,发送HTTP请求失败：{}", e);
        } catch (ServiceException e) {
            // log.error("退款异常,服务返回状态异常：{}", e);
        } catch (MalformedMessageException e) {
            //  log.error("退款异常,服务返回成功，返回体类型不合法，或者解析返回体失败：{}", e);
        }
        return null;
    }
}
