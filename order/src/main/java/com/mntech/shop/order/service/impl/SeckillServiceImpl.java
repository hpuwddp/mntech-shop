package com.mntech.shop.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.db.sql.Order;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mntech.shop.common.Constants.RocketMQConstants;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.dao.SeckillActivityMapper;
import com.mntech.shop.order.dao.SeckillOrderMapper;
import com.mntech.shop.order.dao.SeckillProductMapper;
import com.mntech.shop.order.dto.InvenrotyTaskDto;
import com.mntech.shop.order.dto.SeckillOrderMessage;
import com.mntech.shop.order.dto.StreamKeys;
import com.mntech.shop.order.dto.request.OrderQueryRequest;
import com.mntech.shop.order.dto.request.SeckillBuyRequest;
import com.mntech.shop.order.dto.response.*;
import com.mntech.shop.common.enums.OrderStatus;
import com.mntech.shop.order.model.*;
import com.mntech.shop.order.service.ISeckillService;
import com.mntech.shop.order.util.SnowflakeIdWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeckillServiceImpl implements ISeckillService {

    private final SeckillActivityMapper activityMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final SeckillOrderMapper seckillOrderMapper;
    private final StringRedisTemplate redisTemplate;
    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;
    private static final String SECKILL_STOCK_KEY = "seckill:stock:";
    private static final String SECKILL_USER_KEY = "seckill:user:";
    private static final String SECKILL_STOCK_PREFIX = "seckill:stock:";
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;
    // ==================== Redis Key 前缀 ====================
    private static final String SECKILL_ACTIVITY_PREFIX = "seckill:activity:";
    private static final String SECKILL_PRODUCT_PREFIX = "seckill:product:";
    private static final String SECKILL_PRODUCT_STOCK_PREFIX = "seckill:productstock:";
    // ==================== 缓存过期时间 ====================
    private static final long CACHE_EXPIRE_HOURS = 24;
    // ==================== 用户端 ====================
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public List<SeckillActivityResponse> getOngoingActivities() {
        // 查询进行中的活动
        LambdaQueryWrapper<SeckillActivityModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillActivityModel::getStatus, "APPROVED")
                .eq(SeckillActivityModel::getDeleted, 0)
                .le(SeckillActivityModel::getStartTime, LocalDateTime.now())
                .ge(SeckillActivityModel::getEndTime, LocalDateTime.now())
                .orderByAsc(SeckillActivityModel::getCreatedAt);

        List<SeckillActivityModel> activities = activityMapper.selectList(wrapper);

        if (CollectionUtil.isEmpty(activities)) {
            return new ArrayList<>();
        }

        return activities.stream()
                .map(this::convertToActivityResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SeckillActivityResponse getActivityDetailForUser(String activityId) {
        SeckillActivityModel activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        return convertToActivityResponse(activity);
    }

//    @Override
//    @Transactional
//    public SeckillOrderResponse seckillBuy(SeckillBuyRequest request) {
//        String userId = request.getUserId();
//        System.out.println("userid---"+userId);
//        String seckillProductId = request.getSeckillProductId();
//        Integer quantity = request.getQuantity();
//
//        // ========== 1. 从 Redis 获取商品信息 ==========
//String productKey = SECKILL_PRODUCT_PREFIX + seckillProductId;
//        String productJson = redisTemplate.opsForValue().get(productKey);
//        SeckillProductModel product = null;
//        if (productJson != null) {
//            product = JSONUtil.toBean(productJson, SeckillProductModel.class);
//        }
//        if (product == null) {
//            // 缓存没有，查数据库
//            Wrapper<SeckillProductModel> wrapper = Wrappers.<SeckillProductModel>query().eq("product_id", seckillProductId);
//            List<SeckillProductModel> list = seckillProductMapper.selectList(wrapper);
//            if (list!=null && list.size()>0){
//                product =list.get(0);
//            }
//            else
//            {
//                throw new BusinessException("秒杀商品不存在");
//            }
//
//            // 放入缓存
//            redisTemplate.opsForValue().set(productKey, JSONUtil.toJsonStr(product), 10, TimeUnit.MINUTES);
//        }
//
//        // ========== 2. 从 Redis 获取活动信息 ==========
//String activityKey = SECKILL_ACTIVITY_PREFIX + product.getActivityId();
//        String activityJson = redisTemplate.opsForValue().get(activityKey);
//        SeckillActivityModel activity = null;
//        if (activityJson != null) {
//            activity = JSONUtil.toBean(activityJson, SeckillActivityModel.class);
//        }
//        if (activity == null) {
//            activity = activityMapper.selectById(product.getActivityId());
//            if (activity == null) {
//                throw new BusinessException("活动不存在");
//            }
//            redisTemplate.opsForValue().set(activityKey, JSONUtil.toJsonStr(activity), 10, TimeUnit.MINUTES);
//        }
//
//        // ========== 3. 校验活动时间 ==========
//        LocalDateTime now = LocalDateTime.now();
//        if (now.isBefore(activity.getStartTime())) {
//            throw new BusinessException("秒杀活动尚未开始");
//        }
//        if (now.isAfter(activity.getEndTime())) {
//            throw new BusinessException("秒杀活动已结束");
//        }
//
//        // ========== 4. Redis 检查库存 ==========
//        String stockKey = SECKILL_STOCK_KEY + seckillProductId;
//        Object stockObj = redisTemplate.opsForValue().get(stockKey);
//        Integer remainingStock = null;
//        if (stockObj != null) {
//            remainingStock = Integer.parseInt(stockObj.toString());
//        }
//        if (remainingStock == null) {
//            // Redis没有库存，从数据库加载
//            remainingStock = product.getRemainingStock();
//            redisTemplate.opsForValue().set(stockKey, String.valueOf(remainingStock), 10, TimeUnit.MINUTES);
//        }
//        if (remainingStock == null || remainingStock < quantity) {
//            throw new BusinessException("库存不足");
//        }
//
//        // ========== 5. Redis 检查限购 ==========
//        String userKey = SECKILL_USER_KEY + seckillProductId + ":" + userId;
//        Object boughtObj = redisTemplate.opsForValue().get(userKey);
//        Integer bought = boughtObj != null ? Integer.parseInt(boughtObj.toString()) : 0;
//        Integer perUserLimit = product.getPerUserLimit() != null ? product.getPerUserLimit() : 1;
//
////if (bought >= perUserLimit) {
////            throw new BusinessException("您已达到限购数量");
////        }
//
//        // ========== 6. Redis 扣减库存（原子操作） ==========
//        Long newStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
//        if (newStock == null || newStock < 0) {
//            // 库存不足，回滚
//            redisTemplate.opsForValue().increment(stockKey, quantity);
//            throw new BusinessException("库存不足");
//        }
//
//        try {
//            //待测试。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
//            //此时 redis里预扣减库存成功 不代表确实数据库里可以扣减 先把任务放在redis 里 然后另外的定时任务去按顺序拿出来进行数据库扣减操作 有序
//            String inventoryTask = redisTemplate.opsForValue().get(SECKILL_PRODUCT_STOCK_PREFIX);
//            if (inventoryTask != null) {
//                List<InvenrotyTaskDto> invenrotyTaskDtos = JSONUtil.toList(inventoryTask, InvenrotyTaskDto.class);
//                if (invenrotyTaskDtos!=null && invenrotyTaskDtos.size()>0){
//                    InvenrotyTaskDto invenrotyTaskDto=new InvenrotyTaskDto();
//                    invenrotyTaskDto.setNum(quantity);
//                    invenrotyTaskDto.setProduct_id(seckillProductId);
//                    invenrotyTaskDto.setUser_id(userId);
//                    invenrotyTaskDtos.add(invenrotyTaskDto);
//                    redisTemplate.opsForValue().set(SECKILL_PRODUCT_STOCK_PREFIX, JSONUtil.toJsonStr(invenrotyTaskDtos), 10, TimeUnit.MINUTES);
//                    System.out.println(JSONUtil.toJsonStr(invenrotyTaskDtos));
//                }
//            }
//            else
//            {
//                List<InvenrotyTaskDto> invenrotyTaskDtos=new ArrayList<>();
//                InvenrotyTaskDto invenrotyTaskDto=new InvenrotyTaskDto();
//                invenrotyTaskDto.setNum(quantity);
//                invenrotyTaskDto.setProduct_id(seckillProductId);
//                invenrotyTaskDto.setUser_id(userId);
//                invenrotyTaskDtos.add(invenrotyTaskDto);
//                redisTemplate.opsForValue().set(SECKILL_PRODUCT_STOCK_PREFIX, JSONUtil.toJsonStr(invenrotyTaskDtos), 10, TimeUnit.MINUTES);
//                System.out.println(JSONUtil.toJsonStr(invenrotyTaskDtos));
//            }
//
//
////            //默认扣减成功
////
////            // ========== 7. 创建秒杀订单 ==========
////            SeckillOrderModel order = new SeckillOrderModel();
////            order.setId(snowflakeIdWorker.generateSekillOrderCode());
////            order.setActivityId(product.getActivityId());
////            order.setSeckillProductId(seckillProductId);
////            order.setUserId(userId);
////            order.setQuantity(quantity);
////            order.setSeckillPrice(product.getSeckillPrice());
////            order.setStatus(OrderStatus.PENDING.name());
////            order.setCreatedAt(LocalDateTime.now());
////            order.setUpdatedAt(LocalDateTime.now());
////            order.setDeleted(0);
////             seckillOrderMapper.insert(order);  // ✅ 保存订单到数据库
//
//            // ========== 8. 更新用户限购记录（Redis） ==========
//            redisTemplate.opsForValue().increment(userKey, quantity);
//            redisTemplate.expire(userKey, 3600, TimeUnit.SECONDS);
//
////            // ========== 9. 扣减数据库库存（乐观锁） ==========
////            int affected = seckillProductMapper.decreaseStock(seckillProductId, quantity, product.getVersion());
////            if (affected == 0) {
////                // 回滚Redis库存
////                redisTemplate.opsForValue().increment(stockKey, quantity);
////                throw new BusinessException("库存扣减失败，请重试");
////            }
////
////            log.info("秒杀下单成功: userId={}, productId={}, quantity={}", userId, seckillProductId, quantity);
////
////            // ========== 10. 发送 RocketMQ 延迟消息 ==========
////            sendOrderTimeoutMessage(order);
//
//            // ========== 11. 构建响应 ==========
//            SeckillOrderResponse response = new SeckillOrderResponse();
////            response.setOrderId(order.getId());
////            response.setSeckillProductId(seckillProductId);
////            response.setProductName(product.getProductName());
////            response.setProductImage(product.getImageUrl());
////            response.setSeckillPrice(product.getSeckillPrice());
////            response.setQuantity(quantity);
////            response.setTotalAmount(product.getSeckillPrice().multiply(BigDecimal.valueOf(quantity)));
////            response.setStatus(OrderStatus.PENDING.name());
////            response.setStatusDesc(OrderStatus.valueOf(OrderStatus.PENDING.name()).getDescription());
////            response.setCreatedAt(order.getCreatedAt());
//
//            return response;
//
//        } catch (Exception e) {
//            // 异常时恢复Redis库存
//            redisTemplate.opsForValue().increment(stockKey, quantity);
//            log.error("秒杀下单失败", e);
//            throw new BusinessException("下单失败，请重试");
//        }
//    }

    /**
     * 20261119只负责将其保存进入redis缓存
     * @param request
     * @return
     */
    @Override
    @Transactional
    public SeckillOrderResponse seckillBuy(SeckillBuyRequest request) {
        String userId = request.getUserId();
        String seckillProductId = request.getSeckillProductId();
        String activityId=request.getActivityId();
        Integer quantity = request.getQuantity();

        // ===== 1. Redis 预扣库存（原子，防超卖）=====
//        String stockKey = SECKILL_STOCK_KEY + seckillProductId;
//        Long newStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
//        if (newStock == null || newStock < 0) {
//            redisTemplate.opsForValue().increment(stockKey, quantity); // 回滚
//            throw new BusinessException("库存不足");
//        }

        //这个地方还可以加一个限购的逻辑




        // ===== 2. 写入 Stream（原子追加，绝不覆盖）=====
        Map<String, String> msg = new HashMap<>();
        msg.put("productId", seckillProductId);
        msg.put("userId", userId);
        msg.put("activityId", activityId);
        msg.put("num", String.valueOf(quantity));
        msg.put("ts", String.valueOf(System.currentTimeMillis()));

        // MAXLEN ~ 10000: 近似裁剪，保留最近约1万条，防止流无限增长
        StringRecord record = StreamRecords.string(msg) // msg 是 Map<String, String>
                .withStreamKey(StreamKeys.INVENTORY_STREAM);
        RecordId recordId = stringRedisTemplate.opsForStream().add(record);
        log.info("任务入流成功: id={}, msg={}", recordId, msg);

        // ===== 3. 返回（订单由消费者异步建）=====
        SeckillOrderResponse response = new SeckillOrderResponse();
        response.setSeckillProductId(seckillProductId);
        response.setQuantity(quantity);
        response.setStatus("PENDING");
        return response;
    }


    @Override
    public Integer getRemainingStock(String seckillProductId) {
        String key = SECKILL_STOCK_KEY + seckillProductId;
Object stock = redisTemplate.opsForValue().get(key);
        if (stock != null) {
            return Integer.parseInt(stock.toString());
        }
        // Redis 没有则查数据库
        SeckillProductModel product = seckillProductMapper.selectById(seckillProductId);
        return product != null ? product.getRemainingStock() : 0;
    }

    @Override
    public List<SeckillOrderResponse> getUserSeckillOrders(String userId) {
        LambdaQueryWrapper<SeckillOrderModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillOrderModel::getUserId, userId)
                .eq(SeckillOrderModel::getDeleted, 0)
                .orderByDesc(SeckillOrderModel::getCreatedAt);

        List<SeckillOrderModel> orders = seckillOrderMapper.selectList(wrapper);

        if (CollectionUtil.isEmpty(orders)) {
            return new ArrayList<>();
        }

        List<SeckillOrderResponse> result = new ArrayList<>();
        for (SeckillOrderModel order : orders) {
            SeckillOrderResponse response = convertToOrderResponse(order);
            result.add(response);
        }
        return result;
    }

    @Override
    public PageResponse<SeckillOrderResponse> getUserSeckillOrders(OrderQueryRequest pageRequest) {
        // 1. 设置分页参数
        int page = pageRequest.getPage() == null || pageRequest.getPage() < 1 ? 1 : pageRequest.getPage();
        int size = pageRequest.getSize() == null || pageRequest.getSize() < 1 ? 20 : pageRequest.getSize();
        size = Math.min(size, 100);  // 限制最大100条，防止恶意请求


        List<SeckillOrderModel> orders;
        PageHelper.startPage(page, size);
        String status = pageRequest.getStatus();
        String userId = pageRequest.getUserId();


        if (status == null || "all".equalsIgnoreCase(status)) {
            orders = seckillOrderMapper.selectByUserId(pageRequest.getUserId());
        } else {
            orders = seckillOrderMapper.selectByUserIdAndStatus(userId, status);
        }

        List<SeckillOrderResponse> result = new ArrayList<>();

        for (SeckillOrderModel order : orders) {
            SeckillOrderResponse response = convertToOrderResponse(order);
            result.add(response);
        }
        PageInfo<SeckillOrderModel> pageInfo = new PageInfo<>(orders);
        // 5. 构建分页响应
        PageResponse<SeckillOrderResponse> response = new PageResponse<>();
        response.setRecords(result);
        response.setTotal(pageInfo.getTotal());
        response.setPage(page);
        response.setSize(size);
        response.setPages(pageInfo.getPages());
        response.setHasNext(page < pageInfo.getPages());
        response.setHasPrev(page > 1);

        return response;
    }

    /**
     * 转换订单响应（包含商品信息）
     */
    private SeckillOrderResponse convertToOrderResponse(SeckillOrderModel order) {
        SeckillOrderResponse response = new SeckillOrderResponse();
        BeanUtil.copyProperties(order, response);
        response.setOrderId(order.getId());
        response.setStatusDesc(order.getStatusDesc());
        response.setTotalAmount(order.getSeckillPrice().multiply(BigDecimal.valueOf(order.getQuantity())));

        // ✅ 查询商品信息
        SeckillProductModel product = seckillProductMapper.selectByIdWithProduct(order.getSeckillProductId());
        if (product != null) {
            response.setProductName(product.getProductName());
            response.setProductImage(product.getImageUrl());
            // response.setSkuName(product.getSkuName());
        } else {
            response.setProductName("商品已下架");
            response.setProductImage(null);
            // response.setSkuName("默认规格");
        }

        // ✅ 计算剩余支付时间（仅待支付状态）
        if (OrderStatus.PENDING.name().equals(order.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireTime = order.getCreatedAt().plusMinutes(15);
            if (now.isAfter(expireTime)) {
                response.setRemainingPayTime(0L);
            } else {
                response.setRemainingPayTime(Duration.between(now, expireTime).getSeconds());
            }
        } else {
            response.setRemainingPayTime(0L);
        }

        return response;
    }

    @Override
    @Transactional
    public void cancelSeckillOrder(String orderId) {
        SeckillOrderModel seckillOrderModel = seckillOrderMapper.selectById(orderId);
        if (seckillOrderModel == null) {
            throw new BusinessException("订单不存在");
        }

        if (!OrderStatus.PENDING.name().equals(seckillOrderModel.getStatus())) {
            throw new BusinessException("当前订单状态不能取消");
        }
        // 更新订单状态
        seckillOrderModel.setStatus(OrderStatus.CANCELLED.name());
        seckillOrderModel.setUpdatedAt(LocalDateTime.now());
        seckillOrderMapper.updateById(seckillOrderModel);
        // 回退Redis库存
        String stockKey = SECKILL_STOCK_KEY + seckillOrderModel.getSeckillProductId();
        redisTemplate.opsForValue().increment(stockKey, seckillOrderModel.getQuantity());
// 查询秒杀商品（取最新库存和版本号）
        SeckillProductModel seckillProduct = seckillProductMapper.selectById(seckillOrderModel.getSeckillProductId());
        if (seckillProduct == null) {
            throw new BusinessException("秒杀商品不存在");
        }
        // 回退数据库库存（乐观锁校验，失败重试一次）
        int release = seckillProductMapper.increaseStock(seckillOrderModel.getSeckillProductId(), seckillOrderModel.getQuantity(), seckillProduct.getVersion());
        if (release == 0) {
            SeckillProductModel latest = seckillProductMapper.selectById(seckillOrderModel.getSeckillProductId());
            if (latest != null) {
                release = seckillProductMapper.increaseStock(seckillOrderModel.getSeckillProductId(), seckillOrderModel.getQuantity(), latest.getVersion());
            }
        }
        if (release == 0) {
            throw new BusinessException("库存回退失败，请稍后重试");
        }


        log.info("取消秒杀订单成功: orderId={}", orderId);
    }

    @Override
    @Transactional
    public SeckillOrderResponse paySeckillOrder(String orderId) {
        SeckillOrderModel order = seckillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能支付");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.PAID.name());
        order.setUpdatedAt(LocalDateTime.now());
        seckillOrderMapper.updateById(order);

        log.info("秒杀订单支付成功: orderId={}", orderId);

        SeckillOrderResponse response = new SeckillOrderResponse();
        BeanUtil.copyProperties(order, response);
        response.setOrderId(order.getId());
        response.setStatusDesc("已支付");
        return response;
    }



    // ==================== 私有方法 ====================

    private SeckillActivityResponse convertToActivityResponse(SeckillActivityModel activity) {
        if (activity == null) {
            return null;
        }

        // 查询活动商品
        List<SeckillProductModel> products = seckillProductMapper.selectByActivityIdWithProduct(activity.getId());

        SeckillActivityResponse response = new SeckillActivityResponse();
        BeanUtil.copyProperties(activity, response);

        // 统计库存
        int totalStock = 0;
        int totalRemaining = 0;
        List<SeckillProductResponse> productResponses = new ArrayList<>();

        if (CollectionUtil.isNotEmpty(products)) {
            for (SeckillProductModel product : products) {
                totalStock += product.getStock() != null ? product.getStock() : 0;
                totalRemaining += product.getRemainingStock() != null ? product.getRemainingStock() : 0;

                SeckillProductResponse productResponse = new SeckillProductResponse();
                BeanUtil.copyProperties(product, productResponse);
                productResponse.setIsSoldOut(product.getRemainingStock() == null || product.getRemainingStock() <= 0);
                productResponses.add(productResponse);
            }
        }

        response.setProductCount(products.size());
        response.setTotalStock(totalStock);
        response.setTotalRemainingStock(totalRemaining);
        response.setProducts(productResponses);
        response.setIsOngoing("ONGOING".equals(activity.getStatus()));
        response.setIsEnded("ENDED".equals(activity.getStatus()));

        // 计算剩余时间
        if (response.getIsOngoing()) {
            long remaining = java.time.Duration.between(LocalDateTime.now(), activity.getEndTime()).getSeconds();
            response.setRemainingTime(remaining > 0 ? remaining : 0);
        } else {
            response.setRemainingTime(0L);
        }

        return response;
    }

    @Override
    public SeckillOrderResponse getSeckillOrderDetail(String orderId) {
        // 1. 查询订单
        SeckillOrderModel order = seckillOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 2. 查询秒杀商品信息
        SeckillProductModel product = seckillProductMapper.selectByProductIdWithProduct(order.getSeckillProductId(), order.getActivityId());

        // 3. 构建响应
        SeckillOrderResponse response = new SeckillOrderResponse();
        response.setOrderId(order.getId());
        response.setSeckillProductId(order.getSeckillProductId());
        response.setSeckillPrice(order.getSeckillPrice());
        response.setQuantity(order.getQuantity());
        response.setTotalAmount(order.getSeckillPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
        response.setStatus(order.getStatus());
        response.setStatusDesc(order.getStatusDesc());
        response.setCreatedAt(order.getCreatedAt());

        response.setReceiverAddress(order.getReceiverAddress());
        response.setReceiverName(order.getReceiverName());
        response.setReceiverPhone(order.getReceiverPhone());

        if (product != null) {
            response.setProductName(product.getProductName());
            response.setProductImage(product.getImageUrl());
        }

        // 4. ✅ 如果是待支付状态，计算剩余支付时间（15分钟超时）
        if (OrderStatus.PENDING.name().equals(order.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireTime = order.getCreatedAt().plusMinutes(15);

            if (now.isAfter(expireTime)) {
                // 已超时，自动取消
                order.setStatus(OrderStatus.CANCELLED.name());
                order.setUpdatedAt(now);
                seckillOrderMapper.updateById(order);

// 回退库存（商品存在时才回退）
                if (product != null) {
                    seckillProductMapper.increaseStock(order.getSeckillProductId(), order.getQuantity(), product.getVersion());
                }

                response.setStatus(OrderStatus.CANCELLED.name());
                response.setStatusDesc("已超时取消");
                response.setRemainingPayTime(0L);
            } else {
                long remaining = java.time.Duration.between(now, expireTime).getSeconds();
                response.setRemainingPayTime(remaining);
            }
        } else {
            response.setRemainingPayTime(0L);
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrderAndDecreaseStock(String userId, String productId, int num,String activityId,BigDecimal seckillPrice) {
        // 1) 先扣库存（条件更新，防超卖）
        int affected = seckillProductMapper.decreaseStockRedis(productId, num);
        if (affected == 0) {
            throw new BusinessException("DB库存不足");   // 事务回滚，不建订单
        }


        // 2) 扣成功后再建订单
        SeckillOrderModel order = new SeckillOrderModel();
        order.setId(snowflakeIdWorker.generateSekillOrderCode());
        order.setUserId(userId);
        order.setSeckillProductId(productId);
        order.setQuantity(num);
        order.setStatus("PENDING");
        order.setActivityId(activityId);
        order.setSeckillPrice(seckillPrice.multiply(BigDecimal.valueOf(num)));
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        seckillOrderMapper.insert(order);
    }

    /**
     * 发送订单超时检查消息（延迟20分钟）
     */
    private void sendOrderTimeoutMessage(SeckillOrderModel order) {
        try {
//            SeckillOrderMessage messageData = SeckillOrderMessage.builder()
//                    .orderId(order.getId())
//                    .userId(order.getUserId())
//                    .seckillProductId(order.getSeckillProductId())
//                    .activityId(order.getActivityId())
//                    .quantity(order.getQuantity())
//                    .createTime(System.currentTimeMillis())
//                    .messageType(OrderStatus.PENDING.name())
//                    .build();
//            Message<SeckillOrderMessage> message = MessageBuilder
//                    .withPayload(messageData)
//                    .build();
//            // 发送延迟消息，延迟级别 14 = 20分钟
//            String dest = RocketMQConstants.SECKILL_ORDER_TOPIC + ":" + RocketMQConstants.ORDER_PENDING_TAG;
//            rocketMQTemplate.syncSend(
//                    dest,
//                    message,
//                    3000,
//                    RocketMQConstants.DELAY_LEVEL_20_MIN
//            );

            log.info("发送订单超时检查消息成功: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("发送订单超时检查消息失败: orderId={}", order.getId(), e);
            // 不抛异常，避免影响主流程
        }
    }


    private String getOrderStatusDesc(String status) {
        if (status == null) return "未知";
       return OrderStatus.valueOf(status).getDescription();
    }
}