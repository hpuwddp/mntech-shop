package com.mntech.shop.order.service.impl;

//import com.mntech.shop.order.config.WechatPayConfig;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mntech.shop.common.Constants.RocketMQConstants;
import com.mntech.shop.common.Feign.InventoryFeignClient;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.config.WechatPayProperties;
import com.mntech.shop.order.dao.*;
import com.mntech.shop.order.dto.SeckillOrderMessage;
import com.mntech.shop.order.dto.request.AddressRequest;
import com.mntech.shop.order.dto.request.CreateOrderRequest;
import com.mntech.shop.order.dto.request.OrderQueryRequest;
import com.mntech.shop.order.dto.response.*;
import com.mntech.shop.common.enums.OrderStatus;
import com.mntech.shop.order.model.*;
import com.mntech.shop.order.service.ICouponService;
import com.mntech.shop.order.service.IDistributionService;
import com.mntech.shop.order.service.IPayService;
import com.mntech.shop.order.util.RedisLockUtil;
import com.mntech.shop.pojo.dto.OrderMessage;
import com.mntech.shop.pojo.dto.request.BatchReduceStockRequest;
import com.mntech.shop.pojo.dto.request.BatchStockRequest;
import com.mntech.shop.pojo.dto.request.SkuStockUpdate;
import com.mntech.shop.pojo.dto.response.Result;
import com.mntech.shop.pojo.model.OrderItemModel;
import com.mntech.shop.pojo.model.OrderModel;
import com.mntech.shop.pojo.model.SkuModel;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.RSAPublicKeyConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
//import com.wechat.pay.java.service.partnerpayments.jsapi.model.Transaction;
import com.wechat.pay.java.core.util.PemUtil;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.producer.SendCallback;
//import org.apache.rocketmq.client.producer.SendResult;
//import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayServiceImpl implements IPayService {
private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;
    private final CartMapper cartMapper;
    private final SkuMapper skuMapper;
private final ICouponService couponService;
    private final IDistributionService distributionService;

    private final SeckillOrderMapper seckillOrderMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final WechatPayProperties wechatPayProperties;
    private final InventoryFeignClient inventoryFeignClient;
    /*******redis*******/

    private final StringRedisTemplate redisTemplate;

    private static final String Stock_Prefix = "stock:sku:";
    //一个用户在一定间隔内不容许重复提交
    private static final String Order_User_Locked_Prefix = "order:lock:user:";
    private static final String Order_NO_Key = "order:no:seq";

//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private RedisLockUtil redisLockUtil;
    @Autowired
    private RedissonClient redissonClient;
    @Override
    @Transactional
    public WechatPayResponse createOrderAndPay(CreateOrderRequest request) {
        try {

            // 1. 生成订单号
            String orderNo = generateOrderNo();

// 2. 计算总金额
            BigDecimal totalAmount = request.getItems().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 2.1 计算优惠券抵扣（不核销，校验失败直接抛出，无券副作用）
            CouponCalcResult couponCalc = couponService.calcCouponDiscount(
                    request.getUserId(), request.getUserCouponId(), totalAmount, request.getItems());
            BigDecimal payAmount = totalAmount.subtract(couponCalc.getDiscountAmount());
            if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
                payAmount = new BigDecimal("0.01");
            }

            // 2. 保存订单
            OrderModel order = new OrderModel();
            order.setOrderNo(orderNo);
            order.setUserId(request.getUserId());
            order.setTotalAmount(totalAmount);
            order.setDiscountAmount(couponCalc.getDiscountAmount());
            order.setCouponId(request.getUserCouponId());
            order.setCouponName(couponCalc.getCouponName());
            order.setPayAmount(payAmount);
            order.setStatus(OrderStatus.PENDING.name());
            order.setReceiverName(request.getReceiverName());
            order.setReceiverPhone(request.getReceiverPhone());
            order.setReceiverAddress(request.getReceiverAddress());
            order.setPayType(request.getPayType());
            order.setRemark(request.getRemark());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            order.setDeleted(0);
            orderMapper.insert(order);

// 4. 保存订单明细
            for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
                OrderItemModel item = new OrderItemModel();
                item.setOrderNo(orderNo);
                item.setProductId(itemReq.getProductId());
                item.setSkuId(itemReq.getSkuId());
                item.setProductName(itemReq.getProductName());
                item.setSkuName(itemReq.getSkuName());
                item.setProductImage(itemReq.getProductImage());
                item.setQuantity(itemReq.getQuantity());
                item.setPrice(itemReq.getPrice());
item.setTotalPrice(itemReq.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
                item.setCreatedAt(LocalDateTime.now());
                item.setDeleted(0);
                orderItemMapper.insert(item);
            }
            // 2.2 核销优惠券（订单与明细已落库；若券异常在此抛出，事务提交后订单仍为 PENDING，与既有语义一致）
            couponService.applyCouponForOrder(
                    request.getUserId(), request.getUserCouponId(), orderNo, totalAmount, request.getItems());
            // 3. 调用微信支付统一下单
            PrepayRequest prepayRequest = new PrepayRequest();
            prepayRequest.setAppid(wechatPayProperties.getAppId());
            prepayRequest.setMchid(wechatPayProperties.getMerchantId());
            prepayRequest.setDescription(request.getItems().get(0).getProductName() + "等" + request.getItems().size() + "件商品");
            prepayRequest.setOutTradeNo(orderNo);
            prepayRequest.setNotifyUrl(wechatPayProperties.getNotifyUrl());
// 设置金额
            Amount amount = new Amount();
            amount.setTotal(order.getPayAmount().multiply(new BigDecimal(100)).intValue());
            // amount.setTotal(1);
            amount.setCurrency("CNY");
            prepayRequest.setAmount(amount);

            UserModel userModel = userMapper.selectById(request.getUserId());
// 设置支付者
            Payer payer = new Payer();
            payer.setOpenid(userModel.getOpenid());
            prepayRequest.setPayer(payer);

// ✅ 使用微信支付公钥配置（替代 RSAAutoCertificateConfig）
            Config config = new RSAPublicKeyConfig.Builder()
                    .merchantId(wechatPayProperties.getMerchantId())
                    .merchantSerialNumber(wechatPayProperties.getMerchantSerialNumber())
                    .privateKeyFromPath(wechatPayProperties.getPrivateKeyPath())
                    .apiV3Key(wechatPayProperties.getApiV3Key())
                    // 🔑 新增：微信支付公钥文件路径（从商户平台下载）
                    .publicKeyFromPath(wechatPayProperties.getPublicKeyPath())
                    // 🔑 新增：微信支付公钥ID（从商户平台获取）
                    .publicKeyId(wechatPayProperties.getPublicKeyId())
                    .build();

// 调用统一下单API
            JsapiService jsapiService = new JsapiService.Builder().config(config).build();
            PrepayResponse response = jsapiService.prepay(prepayRequest);

            String randStr = UUID.randomUUID().toString().replace("-", "");
            // 4. 构建小程序调起支付参数
            return WechatPayResponse.builder()
                    .orderNo(orderNo)
                    .success(true)
                    .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                    .nonceStr(randStr)
                    .packageStr("prepay_id=" + response.getPrepayId())
                    .signType("RSA")
                    .paySign(generatePaySign(orderNo, response.getPrepayId(), randStr))
                    .build();

        } catch (Exception e) {
            log.error("创建支付订单失败", e);
            return WechatPayResponse.builder()
                    .success(false)
                    .message("支付下单失败：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 更新订单
     * 地址
     *
     * @param orderNo
     * @param request
     */
    @Override
    @Transactional
    public void updateAddress(String orderNo, AddressRequest request) {
        if (orderNo.contains("SEO")) {
            //秒杀订单
            SeckillOrderModel seckillOrderModel = seckillOrderMapper.selectById(orderNo);
            if (seckillOrderModel == null) {
                throw new RuntimeException("秒杀订单不存在");
            }
            // 只有待确认状态才能修改地址
            if (!OrderStatus.PENDING.name().equals(seckillOrderModel.getStatus())) {
                throw new RuntimeException("当前秒杀订单状态不允许修改地址");
            }

            seckillOrderModel.setReceiverName(request.getReceiverName());
            seckillOrderModel.setReceiverPhone(request.getReceiverPhone());
            seckillOrderModel.setReceiverAddress(request.getDetailAddress());
            seckillOrderModel.setUpdatedAt(LocalDateTime.now());
            seckillOrderMapper.updateById(seckillOrderModel);
        } else {
            //正常订单
            OrderModel order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            // 只有待确认状态才能修改地址
            if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
                throw new RuntimeException("当前订单状态不允许修改地址");
            }

            order.setReceiverName(request.getReceiverName());
            order.setReceiverPhone(request.getReceiverPhone());
//        order.setReceiverAddress(request.getProvince()+request.getCity()+request.getDistrict()+request.getDetailAddress());
            order.setReceiverAddress(request.getDetailAddress());
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);
        }
    }

    /**
     * ✅ 发起支付
     */
    @Override
    @Transactional
    public WechatPayResponse payOrder(String userId, String orderNo) {
        if (orderNo.contains("SEO")) {
            //秒杀订单
            SeckillOrderModel seckillOrderModel = seckillOrderMapper.selectById(orderNo);
            if (seckillOrderModel == null) {
                throw new RuntimeException("秒杀订单不存在");
            }
//查询金额
            // 3. 调用微信支付统一下单
            PrepayRequest prepayRequest = new PrepayRequest();
            prepayRequest.setAppid(wechatPayProperties.getAppId());
            prepayRequest.setMchid(wechatPayProperties.getMerchantId());
            prepayRequest.setDescription("酒购买");
            prepayRequest.setOutTradeNo(orderNo);
            prepayRequest.setNotifyUrl(wechatPayProperties.getNotifyUrl());

// 设置金额
            Amount amount = new Amount();
            amount.setTotal(seckillOrderModel.getTotalAmount().multiply(new BigDecimal(100)).intValue());
            //amount.setTotal(1);
            amount.setCurrency("CNY");
            prepayRequest.setAmount(amount);

            UserModel userModel = userMapper.selectById(userId);
// 设置支付者
            Payer payer = new Payer();
            payer.setOpenid(userModel.getOpenid());
            prepayRequest.setPayer(payer);

// ✅ 使用微信支付公钥配置（替代 RSAAutoCertificateConfig）
            Config config = new RSAPublicKeyConfig.Builder()
                    .merchantId(wechatPayProperties.getMerchantId())
                    .merchantSerialNumber(wechatPayProperties.getMerchantSerialNumber())
                    .privateKeyFromPath(wechatPayProperties.getPrivateKeyPath())
                    .apiV3Key(wechatPayProperties.getApiV3Key())
                    // 🔑 新增：微信支付公钥文件路径（从商户平台下载）
                    .publicKeyFromPath(wechatPayProperties.getPublicKeyPath())
                    // 🔑 新增：微信支付公钥ID（从商户平台获取）
                    .publicKeyId(wechatPayProperties.getPublicKeyId())
                    .build();

// 调用统一下单API
            JsapiService jsapiService = new JsapiService.Builder().config(config).build();
            PrepayResponse response = jsapiService.prepay(prepayRequest);
            String randStr = UUID.randomUUID().toString().replace("-", "");
            // 4. 构建小程序调起支付参数
            return WechatPayResponse.builder()
                    .orderNo(orderNo)
                    .success(true)
                    .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                    .nonceStr(randStr)
                    .packageStr("prepay_id=" + response.getPrepayId())
                    .signType("RSA")
                    .paySign(generatePaySign(orderNo, response.getPrepayId(), randStr))
                    .build();


        }
        else

        {
            //普通订单
            //查询金额
            OrderModel orderModel = orderMapper.selectByOrderNo(orderNo);

            // 3. 调用微信支付统一下单
            PrepayRequest prepayRequest = new PrepayRequest();
            prepayRequest.setAppid(wechatPayProperties.getAppId());
            prepayRequest.setMchid(wechatPayProperties.getMerchantId());
            prepayRequest.setDescription("酒购买");
            prepayRequest.setOutTradeNo(orderNo);
            prepayRequest.setNotifyUrl(wechatPayProperties.getNotifyUrl());
// 设置金额
            Amount amount = new Amount();
            amount.setTotal(orderModel.getPayAmount().multiply(new BigDecimal(100)).intValue());
            //amount.setTotal(1);
            amount.setCurrency("CNY");
            prepayRequest.setAmount(amount);

            UserModel userModel = userMapper.selectById(userId);
// 设置支付者
            Payer payer = new Payer();
            payer.setOpenid(userModel.getOpenid());
            prepayRequest.setPayer(payer);

// ✅ 使用微信支付公钥配置（替代 RSAAutoCertificateConfig）
            Config config = new RSAPublicKeyConfig.Builder()
                    .merchantId(wechatPayProperties.getMerchantId())
                    .merchantSerialNumber(wechatPayProperties.getMerchantSerialNumber())
                    .privateKeyFromPath(wechatPayProperties.getPrivateKeyPath())
                    .apiV3Key(wechatPayProperties.getApiV3Key())
                    // 🔑 新增：微信支付公钥文件路径（从商户平台下载）
                    .publicKeyFromPath(wechatPayProperties.getPublicKeyPath())
                    // 🔑 新增：微信支付公钥ID（从商户平台获取）
                    .publicKeyId(wechatPayProperties.getPublicKeyId())
                    .build();

// 调用统一下单API
            JsapiService jsapiService = new JsapiService.Builder().config(config).build();
            PrepayResponse response = jsapiService.prepay(prepayRequest);
            String randStr = UUID.randomUUID().toString().replace("-", "");
            // 4. 构建小程序调起支付参数
            return WechatPayResponse.builder()
                    .orderNo(orderNo)
                    .success(true)
                    .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                    .nonceStr(randStr)
                    .packageStr("prepay_id=" + response.getPrepayId())
                    .signType("RSA")
                    .paySign(generatePaySign(orderNo, response.getPrepayId(), randStr))
                    .build();

        }



    }

    /**
     * 只创建订单
     * 不发起支付
     * @param request
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrderOnly(CreateOrderRequest request) {
        String userId = request.getUserId();
        String lockKey = Order_User_Locked_Prefix + userId;
        try {
            // ========== 🔍 日志2：防重复提交 ==========

            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS);

            if (Boolean.FALSE.equals(success)) {
//                log.warn("⚠️ 防重复提交触发: userId={}, key={}", userId, lockKey);
              throw new BusinessException("请勿重复提交订单，请5秒后重试");
            }

//            for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
//
//                checkAndLockSkuStock(itemReq, userId, 5);
//            }
            String orderNo = generateOrderNo();
            request.setOrderNo(orderNo);
               boolean checkResult= checkAndLockSkuStock(request, userId, 3);
            if (checkResult){
                // 生成订单号

// 计算总金额
                BigDecimal totalAmount = request.getItems().stream()
                        .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                // 计算优惠券抵扣（不核销，校验失败直接抛出，事务回滚无副作用）
                CouponCalcResult couponCalc = couponService.calcCouponDiscount(
                        userId, request.getUserCouponId(), totalAmount, request.getItems());
                BigDecimal payAmount = totalAmount.subtract(couponCalc.getDiscountAmount());
                if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    payAmount = new BigDecimal("0.01");
                }
                // 保存订单
                OrderModel order = new OrderModel();
                order.setOrderNo(orderNo);
                order.setUserId(userId);
                order.setTotalAmount(totalAmount);
                order.setDiscountAmount(couponCalc.getDiscountAmount());
                order.setCouponId(request.getUserCouponId());
                order.setCouponName(couponCalc.getCouponName());
                order.setPayAmount(payAmount);
                order.setStatus(OrderStatus.PENDING.name());//待支付
                order.setReceiverName(request.getReceiverName());
                order.setReceiverPhone(request.getReceiverPhone());
                order.setReceiverAddress(request.getReceiverAddress());
                order.setPayType(request.getPayType());
                order.setRemark(request.getRemark());
                order.setCreatedAt(LocalDateTime.now());
                order.setUpdatedAt(LocalDateTime.now());
                order.setDeleted(0);
                orderMapper.insert(order);
                // 保存订单明细
                for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
                    OrderItemModel item = new OrderItemModel();
                    item.setOrderNo(orderNo);
                    item.setProductId(itemReq.getProductId());
                    item.setSkuId(itemReq.getSkuId());
                    item.setProductName(itemReq.getProductName());
                    item.setSkuName(itemReq.getSkuName());
                    item.setProductImage(itemReq.getProductImage());
                    item.setQuantity(itemReq.getQuantity());
                    item.setPrice(itemReq.getPrice());
                    item.setTotalPrice(itemReq.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
item.setCreatedAt(LocalDateTime.now());
                    item.setDeleted(0);
                    item.setUserId(userId);
                    orderItemMapper.insert(item);
                    // log.info("  明细保存成功: SKU={}, 数量={}", itemReq.getSkuId(), itemReq.getQuantity());
                }
                // 核销优惠券（订单与明细已落库；异常时整个事务回滚）
                couponService.applyCouponForOrder(
                        userId, request.getUserCouponId(), orderNo, totalAmount, request.getItems());

                // 删除防重锁
                redisTemplate.delete(lockKey);



//                //异步发送mq  订单超时未支付 延时消息   主要是利用mq的延时队列处理 订单超时未支付
//                OrderMessage  messageData = OrderMessage.builder()
//                        .orderId(order.getId())
//                        .userId(order.getUserId())
//                        .createTime(System.currentTimeMillis())
//                        .messageType(OrderStatus.PENDING.name())
//                        .build();
//                Message<OrderMessage>   message = MessageBuilder
//                        .withPayload(messageData)
//                        .build();
//                // 发送延迟消息，延迟级别 14 = 20分钟
//                String   dest = RocketMQConstants.ORDER_TOPIC + ":" + RocketMQConstants.ORDER_PENDING_TAG;
//                // ✅ 异步发送（不阻塞主流程）
//                rocketMQTemplate.asyncSend(
//                        dest,
//                        message,
//                        new SendCallback() {
//                            @Override
//                            public void onSuccess(SendResult sendResult) {
//                                log.info("✅ MQ消息发送成功: orderId={}, msgId={}",
//                                        order.getId(), sendResult.getMsgId());
//                            }
//
//                            @Override
//                            public void onException(Throwable throwable) {
//                                log.error("❌ MQ消息发送失败: orderId={}", order.getId(), throwable);
//                            }
//                        },
//                        3000,
//                        RocketMQConstants.DELAY_LEVEL_20_MIN
//                );

                return OrderResponse.builder().orderNo(orderNo).build();
            }
            else
            {
                return null;
            }
        }  catch (Exception e) {
            throw e;
        }
    }


    @Override
    @Transactional
    public void handleWechatNotify(String body, String signature, String serial, String timestamp, String nonce) {

        try {
            // 1. 构建请求参数
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(serial)
                    .nonce(nonce)
                    .signature(signature)
                    .timestamp(timestamp)
                    .body(body)
                    .build();

            // 2. ✅ 使用微信支付公钥配置（与统一下单保持一致）

            // 3. 创建通知解析器
            NotificationConfig notificationConfig = new RSAPublicKeyConfig.Builder()
                    .merchantId(wechatPayProperties.getMerchantId())
                    .merchantSerialNumber(wechatPayProperties.getMerchantSerialNumber())
                    .privateKeyFromPath(wechatPayProperties.getPrivateKeyPath())
                    .apiV3Key(wechatPayProperties.getApiV3Key())
                    .publicKeyFromPath(wechatPayProperties.getPublicKeyPath())   // ✅ 微信支付公钥文件
                    .publicKeyId(wechatPayProperties.getPublicKeyId())                          // ✅ 公钥ID
                    .build();


            NotificationParser parser = new NotificationParser(notificationConfig);

            // 4. 验签并解析
            Transaction transaction = parser.parse(requestParam, Transaction.class);
//                {"amount":{"currency":"CNY","payerCurrency":"CNY","payerTotal":100,"total":100},"appid":"wxa689e4b04bebaeff","attach":"","bankType":"BANK_CREDIT","mchid":"1114709595","outTradeNo":"ORD1782909789826B8BDAF","payer":{"openid":"oOJtS3acJLRLsnUEyKs1rDUSaptU"},"successTime":"2026-07-01T20:43:30+08:00","tradeState":"SUCCESS","tradeStateDesc":"支付成功","tradeType":"JSAPI","transactionId":"4200003147202607018941550107"}
//                success

            // 5. 处理支付结果
            String orderNo = transaction.getOutTradeNo();
            String transactionId = transaction.getTransactionId();
             if ("SUCCESS".equals(transaction.getTradeState().name())) {
                String successTimeStr = transaction.getSuccessTime();
                LocalDateTime payTime = parseWechatTime(successTimeStr);
                if (orderNo.contains("ORD")) {
                    //订单
int result = orderMapper.updatePaySuccess(orderNo, transactionId, payTime);
                    if (result > 0) {
                        log.info("订单支付成功：orderNo={}, transactionId={}", orderNo, transactionId);
                        // 生成分销三级佣金（幂等，仅普通商城订单参与）
                        distributionService.generateCommission(orderNo);
                        //删除购物车信息
                        // 2. ✅ 删除购物车中对应的商品
                        List<OrderItemModel> items = orderItemMapper.selectByOrderNo(orderNo);
                        if (items != null && items.size() > 0) {
                            // 提取 skuId 列表
                            List<String> skuIds = items.stream()
                                    .map(OrderItemModel::getSkuId)
                                    .filter(skuId -> skuId != null && !skuId.isEmpty())
                                    .collect(Collectors.toList());
                            String userId = items.get(0).getUserId();
                            if (skuIds.isEmpty()) {
                                return;
                            }

                            //库存操作
                            List<SkuModel> skuModels = skuMapper.selectByIds(skuIds);
                            //库存操作
                            for (SkuModel skuModel : skuModels) {
                                OrderItemModel matchedItem = items.stream()
                                        .filter(w -> skuModel.getId().equals(w.getSkuId()))
                                        .findFirst()
                                        .orElse(null);

                                if (matchedItem == null) {
                                    log.warn("订单明细中未找到对应的SKU: {}", skuModel.getId());
                                    continue;
                                }

                                int num = matchedItem.getQuantity();
                                //确认库存真实扣减
                                int confirmResult = skuMapper.confirmStock(skuModel.getId(), num, skuModel.getVersion());
                                if (confirmResult > 0) {
                                    log.info("确认SKU库存成功: skuId={}, quantity={}", skuModel.getId(), num);
                                } else {
                                    log.warn("确认SKU库存失败，可能已被处理: skuId={}, quantity={}", skuModel.getId(), num);
                                }
                            }


                            // 批量删除 购物车中信息
                            cartMapper.batchDeleteByUserIdAndSkuIds(userId, skuIds);

                        }

                    }
                } else if (orderNo.contains("SEO")) {
                    //秒杀订单
                    int result = seckillOrderMapper.updatePaySuccess(orderNo, transactionId, payTime);
                    if (result > 0) {
                        log.info("秒杀订单支付成功：orderNo={}, transactionId={}", orderNo, transactionId);
                        //将订单号传递到mq 告知支付成功
//                        try {
//                            SeckillOrderMessage payMessage = SeckillOrderMessage.builder()
//                                    .orderId(orderNo)
//                                    .messageType("PAY")
//                                    .build();
//                            String dest = RocketMQConstants.SECKILL_ORDER_TOPIC + ":" + RocketMQConstants.ORDER_PAY_TAG;
//                            SendResult sendResult = rocketMQTemplate.syncSend(dest, payMessage);
//                            log.info("【MQ发送结果】status={}, msgId={}", sendResult.getSendStatus(), sendResult.getMsgId());
//                        }
//                        catch (Exception ex){
//                            log.error("【MQ发送失败】", ex);
//                        }




//                        //对应秒杀活动里的 秒杀产品库存更改
//                        SeckillOrderModel seckillOrderModel = seckillOrderMapper.selectById(orderNo);
//                        SeckillProductModel seckillProductModel = seckillProductMapper.selectByProductIdWithProduct(seckillOrderModel.getSeckillProductId(), seckillOrderModel.getActivityId());
//
//                        int confirmResult = seckillProductMapper.confirmPay(seckillProductModel.getId(), seckillOrderModel.getQuantity(), seckillProductModel.getVersion());
//                        if (confirmResult > 0) {
//                            log.info("确认SKU库存成功: seckillProductModelId={}, quantity={}", seckillProductModel.getId(), seckillOrderModel.getQuantity());
//
////                            //向rocketmq 发送取消超时订单消息
////                            // 2. 发送支付成功消息（可选，用于后续流程）
////                            SeckillOrderMessage payMessage = SeckillOrderMessage.builder()
////                                    .orderId(orderNo)
////                                    .messageType("PAY")
////                                    .build();
////
////                            rocketMQTemplate.syncSend(RocketMQConstants.SECKILL_ORDER_TOPIC, payMessage);


//                        } else {
//                           // log.warn("确认SKU库存失败，可能已被处理: seckillProductModelId={}, quantity={}", seckillProductModel.getId(), seckillOrderModel.getQuantity());
//                        }

                    }
                }


            }

        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            throw new RuntimeException("支付回调处理失败");
        }
    }

    @Override
    public void testpaySuccess(String orderNo) {
        List<OrderItemModel> items = orderItemMapper.selectByOrderNo(orderNo);
        if (items != null && items.size() > 0) {
            // 提取 skuId 列表
            List<String> skuIds = items.stream()
                    .map(OrderItemModel::getSkuId)
                    .filter(skuId -> skuId != null && !skuId.isEmpty())
                    .collect(Collectors.toList());

            List<SkuModel> skuModels = skuMapper.selectByIds(skuIds);
            //库存操作
            for (SkuModel skuModel : skuModels) {
                OrderItemModel matchedItem = items.stream()
                        .filter(w -> skuModel.getId().equals(w.getSkuId()))
                        .findFirst()
                        .orElse(null);

                if (matchedItem == null) {
                    log.warn("订单明细中未找到对应的SKU: {}", skuModel.getId());
                    continue;
                }

                int num = matchedItem.getQuantity();
                //确认库存真实扣减
                int confirmResult = skuMapper.confirmStock(skuModel.getId(), num, skuModel.getVersion());
                if (confirmResult > 0) {
                    log.info("确认SKU库存成功: skuId={}, quantity={}", skuModel.getId(), num);
                } else {
                    log.warn("确认SKU库存失败，可能已被处理: skuId={}, quantity={}", skuModel.getId(), num);
                }
            }

        }

    }

    @Override
    public String getOrderStatus(String orderNo) {
        OrderModel order = orderMapper.selectByOrderNo(orderNo);
        return order != null ? order.getStatus() : null;
    }

    @Override
    public OrderResponse getOrderDetail(String orderNo) {
        OrderModel order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return null;
        }

        List<OrderItemModel> items = orderItemMapper.selectByOrderNo(orderNo);

        return convertToOrderResponse(order, items);
    }

    @Override
    public PageResponse<OrderResponse> getUserOrders(OrderQueryRequest pageRequest) {
        // 1. 设置分页参数
        int page = pageRequest.getPage() == null || pageRequest.getPage() < 1 ? 1 : pageRequest.getPage();
        int size = pageRequest.getSize() == null || pageRequest.getSize() < 1 ? 20 : pageRequest.getSize();
        size = Math.min(size, 100);  // 限制最大100条，防止恶意请求

        PageHelper.startPage(page, size);

        List<OrderModel> orders = orderMapper.selectByUserId(pageRequest.getUserId());
        List<OrderResponse> result = new ArrayList<>();

        for (OrderModel order : orders) {
            List<OrderItemModel> items = orderItemMapper.selectByOrderNo(order.getOrderNo());
            result.add(convertToOrderResponse(order, items));
        }
        PageInfo<OrderModel> pageInfo = new PageInfo<>(orders);
        // 5. 构建分页响应
        PageResponse<OrderResponse> response = new PageResponse<>();
        response.setRecords(result);
        response.setTotal(pageInfo.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages(pageInfo.getPages());
        response.setHasNext(page < pageInfo.getPages());
        response.setHasPrev(page > 1);

        return response;
    }

    @Override
    public PageResponse<OrderResponse> getUserOrdersByStatus(OrderQueryRequest pageRequest) {
        List<OrderModel> orders;

        // 1. 设置分页参数
        int page = pageRequest.getPage() == null || pageRequest.getPage() < 1 ? 1 : pageRequest.getPage();
        int size = pageRequest.getSize() == null || pageRequest.getSize() < 1 ? 20 : pageRequest.getSize();
        size = Math.min(size, 100);  // 限制最大100条，防止恶意请求

        PageHelper.startPage(page, size);

        String status = pageRequest.getStatus();
        String userId = pageRequest.getUserId();


        if (status == null || "all".equalsIgnoreCase(status)) {
            orders = orderMapper.selectByUserId(userId);
        } else {
            orders = orderMapper.selectByUserIdAndStatus(userId, status);
        }
        // 3. 获取分页信息
        PageInfo<OrderModel> pageInfo = new PageInfo<>(orders);
        List<OrderResponse> orderResponses = convertOrders(orders);
        // 5. 构建分页响应
        PageResponse<OrderResponse> response = new PageResponse<>();
        response.setRecords(orderResponses);
        response.setTotal(pageInfo.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages(pageInfo.getPages());
        response.setHasNext(page < pageInfo.getPages());
        response.setHasPrev(page > 1);

        return response;

    }

@Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo) {
        OrderModel order = orderMapper.selectByOrderNo(orderNo);
        int result = orderMapper.cancelOrder(orderNo);
        if (result > 0) {
            if (order != null && StrUtil.isNotBlank(order.getCouponId())) {
                couponService.refundCouponForOrder(order.getCouponId());
            }
            // 分销佣金作废（幂等）
            distributionService.cancelCommission(orderNo);
        }
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse applyCouponToOrder(String orderNo, String userCouponId) {
        OrderModel order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许更换优惠券");
        }
        List<OrderItemModel> orderItems = orderItemMapper.selectByOrderNo(orderNo);

        String oldCouponId = order.getCouponId();
        // 选择相同券：直接返回当前订单
        if (StrUtil.equals(oldCouponId, userCouponId)) {
            return convertToOrderResponse(order, orderItems);
        }

        // 订单明细转为计价请求项（用于新券适用范围校验）
        List<CreateOrderRequest.OrderItemRequest> itemRequests = new ArrayList<>();
        for (OrderItemModel item : orderItems) {
            CreateOrderRequest.OrderItemRequest req = new CreateOrderRequest.OrderItemRequest();
            req.setProductId(item.getProductId());
            req.setSkuId(item.getSkuId());
            req.setProductName(item.getProductName());
            req.setSkuName(item.getSkuName());
            req.setProductImage(item.getProductImage());
            req.setQuantity(item.getQuantity());
            req.setPrice(item.getPrice());
            itemRequests.add(req);
        }

        if (StrUtil.isBlank(userCouponId)) {
            // 不使用优惠券：返还旧券并清除抵扣
            if (StrUtil.isNotBlank(oldCouponId)) {
                couponService.refundCouponForOrder(oldCouponId);
            }
            // 显式清空数据库中的券字段（MyBatis-Plus 默认非空策略不写 NULL，必须用自定义 SQL 清列）
            orderMapper.clearCoupon(orderNo);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setCouponId(null);
            order.setCouponName(null);
            order.setPayAmount(order.getTotalAmount());
        } else {
            // 先校验并计算新券（不核销），失败时旧券保持不动
            CouponCalcResult calc = couponService.calcCouponDiscount(
                    order.getUserId(), userCouponId, order.getTotalAmount(), itemRequests);
            // 返还旧券（与新券不同才会走到这里）
            if (StrUtil.isNotBlank(oldCouponId)) {
                couponService.refundCouponForOrder(oldCouponId);
            }
            // 核销新券
            couponService.applyCouponForOrder(
                    order.getUserId(), userCouponId, orderNo, order.getTotalAmount(), itemRequests);
            BigDecimal payAmount = order.getTotalAmount().subtract(calc.getDiscountAmount());
            if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
                payAmount = new BigDecimal("0.01");
            }
            order.setDiscountAmount(calc.getDiscountAmount());
            order.setCouponId(userCouponId);
            order.setCouponName(calc.getCouponName());
            order.setPayAmount(payAmount);
        }
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return convertToOrderResponse(order, orderItems);
    }

    @Override
public boolean confirmReceipt(String orderNo) {
        int result = orderMapper.confirmReceipt(orderNo);
        if (result > 0) {
            // 分销佣金入账（PENDING -> AVAILABLE）
            distributionService.settleCommission(orderNo);
        }
        return result > 0;
    }
    // ==================== ✅ 订单统计 ====================

    @Override
    public OrderStatisticsResponse getOrderStatistics(String userId) {
        int all = orderMapper.countAll(userId);
        int pending = orderMapper.countPending(userId);
        int paid = orderMapper.countPaid(userId);
        int shipped = orderMapper.countShipped(userId);
        int completed = orderMapper.countCompleted(userId);

        return OrderStatisticsResponse.builder()
                .all(all)
                .pending(pending)
                .paid(paid)          // 待发货
                .shipped(shipped)    // 待收货
                .completed(completed) // 待评价
                .build();
    }

    @Override
    public WechatPayStatusResponse queryWechatPayStatus(String orderNo) {
        try {
            // 1. 先查询本地订单
            OrderModel order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }

            // 如果本地订单已是支付成功状态，直接返回
            if (OrderStatus.PAID.name().equals(order.getStatus()) || OrderStatus.COMPLETED.name().equals(order.getStatus())) {
                return WechatPayStatusResponse.builder()
                        .outTradeNo(orderNo)
                        .transactionId(order.getTransactionId())
                        .tradeState("SUCCESS")
                        .tradeStateDesc("支付成功")
                        .localStatus(order.getStatus())
                        .isPaid(true)
                        .build();
            }

            // 2. 调用微信支付查询接口
            QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
            queryRequest.setMchid("1723237994");
            queryRequest.setOutTradeNo(orderNo);

            Config config = new RSAAutoCertificateConfig.Builder()
                    .merchantId("1723237994")
                    .merchantSerialNumber("")
                    .privateKeyFromPath("")
                    .apiV3Key("")
                    .build();

            JsapiService jsapiService = new JsapiService.Builder().config(config).build();

            // ✅ 这里使用 partnerpayments 包下的 Transaction
            com.wechat.pay.java.service.payments.model.Transaction transaction = jsapiService.queryOrderByOutTradeNo(queryRequest);

            if (transaction == null) {
                return WechatPayStatusResponse.builder()
                        .outTradeNo(orderNo)
                        .tradeState("NOTPAY")
                        .tradeStateDesc("订单不存在")
                        .localStatus(order.getStatus())
                        .isPaid(false)
                        .build();
            }

            // 3. 解析微信返回的交易状态
            String tradeState = transaction.getTradeState().name();
            String tradeStateDesc = getTradeStateDesc(tradeState);
            String transactionId = transaction.getTransactionId();
            Integer payerTotal = transaction.getAmount().getPayerTotal();

            // 4. 如果微信返回支付成功，更新本地订单状态
            if ("SUCCESS".equals(tradeState)) {
                String successTimeStr = transaction.getSuccessTime();
                LocalDateTime payTime = parseWechatTime(successTimeStr);

int result = orderMapper.updatePaySuccess(orderNo, transactionId, payTime);
                if (result > 0) {
                    log.info("查询支付状态后更新订单成功：orderNo={}, transactionId={}", orderNo, transactionId);
                    // 生成分销三级佣金（幂等）
                    distributionService.generateCommission(orderNo);
                }
            }

            // 5. 重新查询本地订单状态
            OrderModel updatedOrder = orderMapper.selectByOrderNo(orderNo);
            String localStatus = updatedOrder != null ? updatedOrder.getStatus() : order.getStatus();

            return WechatPayStatusResponse.builder()
                    .outTradeNo(orderNo)
                    .transactionId(transactionId)
                    .tradeState(tradeState)
                    .tradeStateDesc(tradeStateDesc)
                    .payerTotal(payerTotal)
                    .successTime(transaction.getSuccessTime())
                    .localStatus(localStatus)
                    .isPaid("SUCCESS".equals(tradeState))
                    .build();

        } catch (Exception e) {
            log.error("查询微信支付状态失败: orderNo={}, error={}", orderNo, e.getMessage(), e);
            // 查询失败时，返回本地订单状态
            OrderModel order = orderMapper.selectByOrderNo(orderNo);
            if (order != null) {
                return WechatPayStatusResponse.builder()
                        .outTradeNo(orderNo)
                        .tradeState("UNKNOWN")
                        .tradeStateDesc("查询失败：" + e.getMessage())
                        .localStatus(order.getStatus())
                        .isPaid(OrderStatus.PAID.name().equals(order.getStatus()) || OrderStatus.COMPLETED.name().equals(order.getStatus()))
                        .build();
            }
            throw new BusinessException("查询支付状态失败：" + e.getMessage());
        }
    }


    // ===== 私有方法 =====

    /**
     * 获取交易状态描述
     */
    private String getTradeStateDesc(String tradeState) {
        if (StrUtil.isBlank(tradeState)) {
            return "未知状态";
        }
        switch (tradeState) {
            case "SUCCESS":
                return "支付成功";
            case "REFUND":
                return "转入退款";
            case "NOTPAY":
                return "未支付";
            case "CLOSED":
                return "已关闭";
            case "REVOKED":
                return "已撤销";
            case "USERPAYING":
                return "用户支付中";
            case "PAYERROR":
                return "支付失败";
            default:
                return tradeState;
        }
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * 生成支付签名（使用商户私钥进行 RSA 签名）
     */
    private String generatePaySign(String orderNo, String prepayId, String randStr) {
        String appId =wechatPayProperties.getAppId();
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        // String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String packageStr = "prepay_id=" + prepayId;

        // 1. 构建签名原文（注意：每个字段后面都有换行符）
        String signStr = appId + "\n" +
                timeStamp + "\n" +
                randStr + "\n" +
                packageStr + "\n";


        // 2. 使用商户私钥进行 RSA 签名
        try {
            // 从文件加载私钥（推荐使用 @Value 注入路径）
            PrivateKey privateKey = loadPrivateKey(wechatPayProperties.getPrivateKeyPath());
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signStr.getBytes(StandardCharsets.UTF_8));

            byte[] signedBytes = signature.sign();
            String paySign = Base64.getEncoder().encodeToString(signedBytes);

            return paySign;

        } catch (Exception e) {
            log.error("生成支付签名失败", e);
            throw new BusinessException("支付签名失败: " + e.getMessage());
        }
    }

    /**
     * 从 PEM 文件加载私钥
     */
    private PrivateKey loadPrivateKey(String privateKeyPath) {
        try {
            String privateKeyContent = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(privateKeyPath)), StandardCharsets.UTF_8);
            return PemUtil.loadPrivateKeyFromString(privateKeyContent);
        } catch (Exception e) {
            log.error("加载私钥失败", e);
            throw new RuntimeException("加载私钥失败: " + e.getMessage());
        }
    }


    private OrderResponse convertToOrderResponse(OrderModel order, List<OrderItemModel> items) {
        List<OrderResponse.OrderItemResponse> itemResponses = new ArrayList<>();
        for (OrderItemModel item : items) {
            OrderResponse.OrderItemResponse itemResp = OrderResponse.OrderItemResponse.builder()
                    .productId(item.getProductId())
                    .skuId(item.getSkuId())
                    .productName(item.getProductName())
                    .skuName(item.getSkuName())
                    .productImage(item.getProductImage())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .totalPrice(item.getTotalPrice())
                    .build();
            itemResponses.add(itemResp);
        }

return OrderResponse.builder()
                .orderNo(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .couponId(order.getCouponId())
                .couponName(order.getCouponName())
                .payAmount(order.getPayAmount())
                .status(order.getStatus())
                .statusDesc(getStatusDesc(order.getStatus()))
                .payTime(order.getPayTime())
                .transactionId(order.getTransactionId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverAddress(order.getReceiverAddress())
                .payType(order.getPayType())
                .remark(order.getRemark())
                .createdAt(order.getCreatedAt())
                .wl(order.getWl())
                .wlCode(order.getWlCode())
                .items(itemResponses)
                .build();
    }

    private String getStatusDesc(String status) {
        if (status == null) return "未知";
        return OrderStatus.valueOf(status).getDescription();
    }

    private List<OrderResponse> convertOrders(List<OrderModel> orders) {
        List<OrderResponse> result = new ArrayList<>();
        for (OrderModel order : orders) {
            List<OrderItemModel> items = orderItemMapper.selectByOrderNo(order.getOrderNo());
            result.add(convertToOrderResponse(order, items));
        }
        return result;
    }

    /**
     * 解析微信时间格式
     * 输入: 2026-07-01T20:54:37+08:00
     * 输出: LocalDateTime
     */
    private LocalDateTime parseWechatTime(String successTimeStr) {
        if (successTimeStr == null || successTimeStr.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            // 微信时间格式: 2026-07-01T20:54:37+08:00
            // 去掉时区后缀，解析为 LocalDateTime
            String timeStr = successTimeStr.replace("+08:00", "").replace("Z", "");
            return LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            log.warn("解析微信支付时间失败: {}", successTimeStr, e);
            return LocalDateTime.now();
        }
    }


    /**
     * 检查并锁定SKU库存（多规格商品）
     * 流程：Redis预扣 → Feign查DB真实库存 → DB乐观锁扣减
     * 此方法不需要事务
     * 20261026注释原有逻辑
     * 注释原因：
     * 目前并发量并未达到一定程度
     * 普通订单没必要这么复杂
     */
//    private  boolean checkAndLockSkuStock(CreateOrderRequest createOrderRequest, String userId, int retryTimes) {
//
//        Map<String, Integer> skuQuantityMap = new HashMap<>();
//        // ========== 第一步：遍历订单，先过 Redis 这一关 ==========
//        List<String> skuIds=new ArrayList<>();
//        boolean whileFlag=true;
//        for (CreateOrderRequest.OrderItemRequest itemReq : createOrderRequest.getItems()) {
//
//            String skuId = itemReq.getSkuId();
//            Integer quantity = itemReq.getQuantity();
//            String stockKey = Stock_Prefix + skuId;
//            skuIds.add(skuId);
//            // 检查Redis中是否存在库存key
//            String redisStockStr = redisTemplate.opsForValue().get(stockKey);
//            if (redisStockStr == null) {
//                log.warn("⚠️ Redis中不存在库存key: {}, 跳过该SKU", stockKey);
//                whileFlag=false;
//               //此处应该立即跳出
//                break;
//            }
//            else
//            {
//                // Redis预扣
//                Long remainingStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
//                if (remainingStock == null || remainingStock < 0) {
//                    whileFlag=false;
//                    break;
//                }
//                else
//                {
//                    skuQuantityMap.put(skuId,quantity);
//                }
//            }
//
//           // log.info("Redis预扣成功: skuId={}, 扣除={}, 剩余={}", skuId, quantity, remainingStock);
//
//        }
//        if (whileFlag){
//            // ========== 第二步：通过Redis后，调用Feign查询DB真实库存 ==========
//
//            BatchStockRequest request = new BatchStockRequest();
//            request.setSkuIds(skuIds);
//
//            Result<?> result = inventoryFeignClient.batchGetStock(request);
//          //  {"code":0,"message":"success","data":{"code":200,"list":[{"id":"eff3376ccaf4f0f9e84352344dd3bacf","productId":"a83d08bac30fa4c60493683f66c43875","skuCode":"SKU338504406569844736","skuName":"测试秒杀sku","originalPrice":0.01,"sellingPrice":0.01,"totalStock":10000,"availableStock":9990,"lockedStock":10,"soldStock":0,"imageUrl":"http://pic.bafangjiuye.site/uploads/2026/07/23/f69481e8463241d4be83a4583e6b07ae.png","status":"ACTIVE","salesCount":1,"updatedAt":"2026-08-06T16:42:13.203591","version":9,"deleted":0}]}}
//
//
//            if (result.getCode() != 0) {
//                System.out.println("查询失败，回滚Redis---");
//                // 查询失败，回滚Redis
//                rollbackRedisStock(skuQuantityMap);
//               return false;
//            }
//            else
//            {
//                String tmpStr=JSONUtil.toJsonStr(result.getData());
//                JSONObject dataObj = JSONUtil.parseObj(tmpStr);
//                List<SkuModel> skuList = dataObj.getBeanList("list", SkuModel.class);
//                boolean checkFlag=true;
//                List<SkuStockUpdate> updateList = new ArrayList<>();
//                //先判断 每一个sku是否数据库里库存有效且都够扣除的
//                for (SkuModel skuModel: skuList){
//                    int totalQuantity =0;
//                    if (!"ACTIVE".equals(skuModel.getStatus())) {
//                        //throw new BusinessException("SKU已失效: " + skuModel.getSkuName());
//                        checkFlag=false;
//                        break;
//                    }
//                    else
//                    {
//                        Integer dbStock = skuModel.getAvailableStock();
//                        //本sku本次下单的数量
////                  totalQuantity = createOrderRequest.getItems().stream()
////                        .mapToInt(CreateOrderRequest.OrderItemRequest::getQuantity)
////                        .sum();
//                        totalQuantity=  skuQuantityMap.get(skuModel.getId());
//                        if (dbStock == null || dbStock < totalQuantity) {
//                            checkFlag=false;
//                            break;
////                    throw new BusinessException("SKU库存不足: " + sku.getSkuName() +
////                            "，当前库存: " + (dbStock == null ? 0 : dbStock) +
////                            "，需要: " + quantity);
//                        }
//                        else
//                        {
//                            updateList.add(new SkuStockUpdate(
//                                    skuModel.getId(),
//                                    totalQuantity,
//                                    skuModel.getVersion()
//                            ));
//                        }
//                    }
//
//                }
//                if (checkFlag){
//                    if (updateList.size() > 0) {
//                            // ========== 2. ✅ 批量查询最新数据（一次查询所有SKU） ==========
//                            List<SkuModel> latestSkuList = skuMapper.selectBatchIds(skuIds);
//                            updateList = updateList.stream()
//                                    .peek(update -> {
//                                        SkuModel latestSku = latestSkuList.stream()
//                                                .filter(sku -> sku.getId().equals(update.getSkuId()))
//                                                .findFirst()
//                                                .orElseThrow(() -> new BusinessException("SKU不存在: " + update.getSkuId()));
//
//                                        if (latestSku.getAvailableStock() == null || latestSku.getAvailableStock() < update.getQuantity()) {
//                                            throw new BusinessException("SKU库存不足: " + latestSku.getSkuName());
//                                        }
//                                        update.setVersion(latestSku.getVersion());
//                                    })
//                                    .collect(Collectors.toList());
//
//
//                            // ========== 4. 执行批量更新 ==========
//                            //int affectedRows = skuMapper.batchReduceStock(updateList);
//
//                            result = inventoryFeignClient.batchDeductStock(updateList);
//
//                            if (result.getCode() != 0) {
//                                System.out.println("批量更新失败，回滚Redis---");
//                                // 查询失败，回滚Redis
//                                rollbackRedisStock(skuQuantityMap);
////                                throw new BusinessException("查询SKU信息失败: " + result.getMessage());
//                                return false;
//                            }
//                            else{
//                                //{"code":0,"message":"success","data":{"code":400,"upnum":0}}
//                                tmpStr=JSONUtil.toJsonStr(result.getData());
//                                dataObj = JSONUtil.parseObj(tmpStr);
//                                int code = dataObj.getInt("code");
//                                if (code==200){
//                                    return true;
//                                }
//                                else {
//                                    return false;
//                                }
//                            }
//                    }
//                    else
//                    {
//                        rollbackRedisStock(skuQuantityMap);
//                        return false;
//                    }
//                }
//                else
//                {
//                    //  ，回滚Redis
//                    rollbackRedisStock(skuQuantityMap);
//                    return false;
//                }
//            }
//
//        }
//        else
//        {
//
//            //回滚已经扣减掉的redis里的库存
//            rollbackRedisStock(skuQuantityMap);
//
//            return false;
//        }
//
//    }

    /**
     * 20261026 修改逻辑
     * @param createOrderRequest
     * @param userId
     * @param retryTimes
     * @return
     * 普通订单防止超卖
     */
    private  boolean checkAndLockSkuStock(CreateOrderRequest createOrderRequest, String userId, int retryTimes) {
        List<SkuStockUpdate> updateList = new ArrayList<>();
        updateList=createOrderRequest.getItems().stream()
                .map(item -> new SkuStockUpdate(item.getSkuId(), item.getQuantity(), null))
                .collect(Collectors.toList());
        BatchReduceStockRequest batchReduceStockRequest=new BatchReduceStockRequest();
        batchReduceStockRequest.setItems(updateList);
        batchReduceStockRequest.setBizNo(createOrderRequest.getOrderNo());
            // ========== 第二步：通过Redis后，调用Feign直接更新对应skuid库存 ==========
        Result<?>  result = inventoryFeignClient.batchDeductStock(batchReduceStockRequest);
        if (result.getCode() != 0) {
            System.out.println("批量更新失败");
            return false;
        }
        else{

                return true;
        }
    }
    private boolean doDeductStock(String skuId,int quantity){
        boolean decuctResult=true;
        SkuModel latestSku = skuMapper.selectById(skuId);
        if (latestSku == null) {
            decuctResult=false;
        }

        int updateResult = skuMapper.reduceStock(
                skuId,
                quantity,
                latestSku.getVersion()
        );

        if (updateResult == 0) {
            decuctResult=false;
        } else {

            log.info("DB扣减成功: skuId={}, quantity={}", skuId, quantity);
        }
        return decuctResult;
    }

    /**
     * 回滚Redis库存（当DB扣减失败时）
     */
    private void rollbackRedisStock(Map<String, Integer> skuQuantityMap) {
        if (skuQuantityMap == null || skuQuantityMap.isEmpty()) {
            return;
        }

        List<Object> results = redisTemplate.executePipelined(
                (RedisCallback<Object>) connection -> {
                    for (Map.Entry<String, Integer> entry : skuQuantityMap.entrySet()) {
                        String stockKey = Stock_Prefix + entry.getKey();
                        connection.stringCommands().incrBy(stockKey.getBytes(), entry.getValue());
                    }
                    return null;
                }
        );

    }
}
