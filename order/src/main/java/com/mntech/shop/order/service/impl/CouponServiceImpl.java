package com.mntech.shop.order.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.dao.CouponMapper;
import com.mntech.shop.order.dao.ProductMapper;
import com.mntech.shop.order.dao.UserCouponMapper;
import com.mntech.shop.order.dto.request.CouponReceiveRequest;
import com.mntech.shop.order.dto.request.CouponUseRequest;
import com.mntech.shop.order.dto.request.CreateOrderRequest;
import com.mntech.shop.order.dto.response.CouponCalcResult;
import com.mntech.shop.order.dto.response.CouponResponse;
import com.mntech.shop.order.model.CouponModel;
import com.mntech.shop.order.model.UserCouponModel;
import com.mntech.shop.order.service.ICouponService;
import com.mntech.shop.pojo.model.ProductModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponServiceImpl implements ICouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final ProductMapper productMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String[] COLORS = {
            "#e4393c", "#ff6b35", "#2196f3", "#9c27b0",
            "#ff9800", "#4caf50", "#e91e63", "#607d8b"
    };

    @Override
    @Transactional
    public CouponResponse receiveCoupon(CouponReceiveRequest request) {
        // 1. 检查优惠券是否存在且可领取
        CouponModel coupon = couponMapper.selectById(request.getCouponId());
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }

        if (!"ACTIVE".equals(coupon.getStatus())) {
            throw new BusinessException("优惠券已下架");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new BusinessException("优惠券不在有效期内");
        }

        // 2. 检查库存
        if (coupon.getTotalQuantity() > 0 && coupon.getRemainingQuantity() <= 0) {
            throw new BusinessException("优惠券已领完");
        }

// 3. 检查用户是否已达到领取上限
        int limit = coupon.getUserLimit() == null ? 1 : coupon.getUserLimit();
        int count = userCouponMapper.countUserCoupon(request.getUserId(), request.getCouponId());
        if (count >= limit) {
            throw new BusinessException("您已达到该优惠券的领取上限");
        }

        // 4. 扣减优惠券库存
        int result = couponMapper.deductCouponStock(request.getCouponId());
        if (result == 0) {
            throw new BusinessException("优惠券领取失败，请重试");
        }

        // 5. 保存用户优惠券
        UserCouponModel userCoupon = new UserCouponModel();
        userCoupon.setUserId(request.getUserId());
        userCoupon.setCouponId(request.getCouponId());
        userCoupon.setCouponCode(generateCouponCode());
        userCoupon.setStatus("UNUSED");
        userCoupon.setStartTime(coupon.getStartTime());
        userCoupon.setEndTime(coupon.getEndTime());
        userCoupon.setCreatedAt(LocalDateTime.now());
        userCoupon.setUpdatedAt(LocalDateTime.now());
        userCoupon.setDeleted(0);
        userCouponMapper.insert(userCoupon);

        log.info("用户 {} 领取优惠券 {} 成功", request.getUserId(), coupon.getName());

        return buildCouponResponse(userCoupon, coupon);
    }

    @Override
    public List<CouponResponse> getUserCoupons(String userId) {
        List<UserCouponModel> userCoupons = userCouponMapper.selectByUserId(userId);
        return buildCouponResponses(userCoupons);
    }

    @Override
    public List<CouponResponse> getAvailableCoupons(String userId) {
        List<UserCouponModel> userCoupons = userCouponMapper.selectAvailableByUserId(userId);
        return buildCouponResponses(userCoupons);
    }

    @Override
    public List<CouponResponse> getUsedCoupons(String userId) {
        // 查询已使用的优惠券
        List<UserCouponModel> userCoupons = userCouponMapper.selectByUserId(userId);
        List<UserCouponModel> usedList = new ArrayList<>();
        for (UserCouponModel uc : userCoupons) {
            if ("USED".equals(uc.getStatus())) {
                usedList.add(uc);
            }
        }
        return buildCouponResponses(usedList);
    }

    @Override
    public List<CouponResponse> getExpiredCoupons(String userId) {
        LocalDateTime now = LocalDateTime.now();
        List<UserCouponModel> userCoupons = userCouponMapper.selectByUserId(userId);
        List<UserCouponModel> expiredList = new ArrayList<>();
        for (UserCouponModel uc : userCoupons) {
            if ("UNUSED".equals(uc.getStatus()) && uc.getEndTime().isBefore(now)) {
                expiredList.add(uc);
            }
        }
        return buildCouponResponses(expiredList);
    }

    @Override
    @Transactional
    public boolean useCoupon(CouponUseRequest request) {
        // 1. 检查用户优惠券
        UserCouponModel userCoupon = userCouponMapper.selectById(request.getUserCouponId());
        if (userCoupon == null) {
            throw new BusinessException("优惠券不存在");
        }

        if (!request.getUserId().equals(userCoupon.getUserId())) {
            throw new BusinessException("无权使用该优惠券");
        }

        if (!"UNUSED".equals(userCoupon.getStatus())) {
            throw new BusinessException("优惠券已使用或已过期");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(userCoupon.getStartTime()) || now.isAfter(userCoupon.getEndTime())) {
            throw new BusinessException("优惠券已过期");
        }

        // 2. 使用优惠券
        int result = userCouponMapper.useCoupon(request.getUserCouponId(), request.getOrderNo());
        if (result == 0) {
            throw new BusinessException("优惠券使用失败");
        }

        // 3. 增加优惠券使用数量
        couponMapper.incrementUsedCount(userCoupon.getCouponId());

        log.info("用户 {} 使用优惠券 {} 成功，订单：{}", request.getUserId(), userCoupon.getCouponId(), request.getOrderNo());

        return true;
    }

    @Override
    public List<CouponResponse> getAvailableToReceive(String userId) {
        List<CouponModel> coupons = couponMapper.selectAvailableCoupons();
        List<CouponResponse> result = new ArrayList<>();

        for (CouponModel coupon : coupons) {
            // 检查用户是否已达到领取上限（未领取/部分领取才继续展示；已领满的券不再展示）
            int limit = coupon.getUserLimit() == null ? 1 : coupon.getUserLimit();
            int count = userCouponMapper.countUserCoupon(userId, coupon.getId());
            if (count >= limit) {
                continue;
            }

            CouponResponse response = buildCouponResponse(coupon);
            response.setReceived(count > 0);
            result.add(response);
        }

        return result;
    }

    @Override
    public int countUserCoupons(String userId) {
        return userCouponMapper.countByUserId(userId);
    }

    @Override
    @Transactional
    public CouponCalcResult calcCouponDiscount(String userId, String userCouponId, BigDecimal orderAmount,
                                               List<CreateOrderRequest.OrderItemRequest> items) {
        if (StrUtil.isBlank(userCouponId)) {
            return CouponCalcResult.builder().discountAmount(BigDecimal.ZERO).build();
        }
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("订单金额无效");
        }

        // 1. 校验用户优惠券实例
        UserCouponModel userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (!userId.equals(userCoupon.getUserId())) {
            throw new BusinessException("无权使用该优惠券");
        }
        if (!"UNUSED".equals(userCoupon.getStatus())) {
            throw new BusinessException("优惠券已使用或已过期");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(userCoupon.getStartTime()) || now.isAfter(userCoupon.getEndTime())) {
            throw new BusinessException("优惠券已过期");
        }

        // 2. 校验优惠券定义
        CouponModel coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (!"ACTIVE".equals(coupon.getStatus())) {
            throw new BusinessException("优惠券已下架");
        }
        // 使用门槛
        if (coupon.getMinAmount() != null && coupon.getMinAmount().compareTo(BigDecimal.ZERO) > 0
                && orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException("订单金额未达到优惠券使用门槛（满"
                    + coupon.getMinAmount().stripTrailingZeros().toPlainString() + "元可用）");
        }
        // 适用范围
        validateApplicableScope(coupon, items);

        // 3. 计算抵扣金额
        BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);

        return CouponCalcResult.builder()
                .discountAmount(discountAmount)
                .couponName(coupon.getName())
                .couponId(coupon.getId())
                .userCouponId(userCouponId)
                .build();
    }

    @Override
    @Transactional
    public CouponCalcResult applyCouponForOrder(String userId, String userCouponId, String orderNo,
                                                BigDecimal orderAmount, List<CreateOrderRequest.OrderItemRequest> items) {
        if (StrUtil.isBlank(userCouponId)) {
            return CouponCalcResult.builder().discountAmount(BigDecimal.ZERO).build();
        }
        // 先校验并计算（不核销），失败时无任何券副作用
        CouponCalcResult calc = calcCouponDiscount(userId, userCouponId, orderAmount, items);
        // 核销用户券（原子更新，仅 UNUSED 可成功）
        int useResult = userCouponMapper.useCoupon(userCouponId, orderNo);
        if (useResult == 0) {
            throw new BusinessException("优惠券使用失败，请重新选择");
        }
        // 回写优惠券使用计数
        couponMapper.incrementUsedCount(calc.getCouponId());
        log.info("订单 {} 使用优惠券 {}（{}）核销成功", orderNo, calc.getCouponId(), calc.getCouponName());
        return calc;
    }

    @Override
    @Transactional
    public boolean refundCouponForOrder(String userCouponId) {
        if (StrUtil.isBlank(userCouponId)) {
            return false;
        }
        UserCouponModel userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            return false;
        }
        int restoreResult = userCouponMapper.restoreCoupon(userCouponId);
        if (restoreResult == 0) {
            return false;
        }
        couponMapper.decrementUsedCount(userCoupon.getCouponId());
        log.info("优惠券 {} 已返还（订单取消/换券）", userCouponId);
        return true;
    }

    // ===== 私有方法 =====

    private String generateCouponCode() {
        return "CP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private List<CouponResponse> buildCouponResponses(List<UserCouponModel> userCoupons) {
        List<CouponResponse> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(userCoupons)) {
            return result;
        }

        for (UserCouponModel uc : userCoupons) {
            // 从关联查询中获取优惠券信息
            CouponModel coupon = uc.getCoupon();
            if (coupon == null) {
                continue;
            }
            result.add(buildCouponResponse(uc, coupon));
        }

        return result;
    }

    private CouponResponse buildCouponResponse(CouponModel coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .type(coupon.getType())
                .value(coupon.getValue())
                .discount(coupon.getDiscount())
                .minAmount(coupon.getMinAmount())
                .maxDiscount(coupon.getMaxDiscount())
                .applicableScope(coupon.getApplicableScope())
                .applicableIds(coupon.getApplicableIds())
                .status("UNUSED")
                .startTime(coupon.getStartTime())
                .endTime(coupon.getEndTime())
                .displayValue(getDisplayValue(coupon))
                .displayCondition(getDisplayCondition(coupon))
                .displayTime(getDisplayTime(coupon))
                .color(getColor(coupon))
                .isExpired(isExpired(coupon))
                .build();
    }

    private CouponResponse buildCouponResponse(UserCouponModel userCoupon, CouponModel coupon) {
        String status = userCoupon.getStatus();
        LocalDateTime now = LocalDateTime.now();

        // 如果状态是未使用但已过期，自动标记为过期
        if ("UNUSED".equals(status) && userCoupon.getEndTime().isBefore(now)) {
            status = "EXPIRED";
        }

        return CouponResponse.builder()
                .id(userCoupon.getId())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .type(coupon.getType())
                .value(coupon.getValue())
                .discount(coupon.getDiscount())
                .minAmount(coupon.getMinAmount())
                .maxDiscount(coupon.getMaxDiscount())
                .applicableScope(coupon.getApplicableScope())
                .applicableIds(coupon.getApplicableIds())
                .status(status)
                .startTime(userCoupon.getStartTime())
                .endTime(userCoupon.getEndTime())
                .usedTime(userCoupon.getUsedTime())
                .couponCode(userCoupon.getCouponCode())
                .displayValue(getDisplayValue(coupon))
                .displayCondition(getDisplayCondition(coupon))
                .displayTime(getDisplayTime(userCoupon))
                .color(getColor(coupon))
                .isExpired("EXPIRED".equals(status) || ("UNUSED".equals(status) && userCoupon.getEndTime().isBefore(now)))
                .build();
    }

    /**
     * 校验优惠券适用范围：订单中所有商品都必须命中
     */
    private void validateApplicableScope(CouponModel coupon, List<CreateOrderRequest.OrderItemRequest> items) {
        String scope = coupon.getApplicableScope();
        if (StrUtil.isBlank(scope) || "all".equalsIgnoreCase(scope)) {
            return;
        }
        if (CollectionUtil.isEmpty(items)) {
            throw new BusinessException("订单明细为空，无法校验优惠券适用范围");
        }

        if ("product".equalsIgnoreCase(scope)) {
            List<String> ids = splitIds(coupon.getApplicableIds());
            if (ids.isEmpty()) {
                throw new BusinessException("优惠券适用范围配置异常");
            }
            for (CreateOrderRequest.OrderItemRequest item : items) {
                if (StrUtil.isBlank(item.getProductId()) || !ids.contains(item.getProductId())) {
                    throw new BusinessException("优惠券不适用于当前订单中的部分商品");
                }
            }
        } else if ("category".equalsIgnoreCase(scope)) {
            List<String> ids = splitIds(coupon.getApplicableIds());
            if (ids.isEmpty()) {
                throw new BusinessException("优惠券适用范围配置异常");
            }
            for (CreateOrderRequest.OrderItemRequest item : items) {
                if (StrUtil.isBlank(item.getProductId())) {
                    throw new BusinessException("优惠券不适用于当前订单中的部分商品");
                }
                ProductModel product = productMapper.selectById(item.getProductId());
                if (product == null || StrUtil.isBlank(product.getCategoryId()) || !ids.contains(product.getCategoryId())) {
                    throw new BusinessException("优惠券不适用于当前订单中的部分商品");
                }
            }
        } else {
            throw new BusinessException("优惠券适用范围配置异常");
        }
    }

    private List<String> splitIds(String ids) {
        List<String> result = new ArrayList<>();
        if (StrUtil.isBlank(ids)) {
            return result;
        }
        for (String id : ids.split(",")) {
            if (StrUtil.isNotBlank(id)) {
                result.add(id.trim());
            }
        }
        return result;
    }

    /**
     * 计算抵扣金额：
     * cash - 满减：min(value, orderAmount)
     * discount - 折扣：orderAmount * (1 - discount/10)，受 maxDiscount 上限约束，且不超过订单金额
     * free_shipping - 包邮：当前订单运费恒为 0，抵扣 0（仅核销标记）
     */
    private BigDecimal calculateDiscount(CouponModel coupon, BigDecimal orderAmount) {
        if ("cash".equalsIgnoreCase(coupon.getType())) {
            BigDecimal value = coupon.getValue() == null ? BigDecimal.ZERO : coupon.getValue();
            return value.min(orderAmount);
        } else if ("discount".equalsIgnoreCase(coupon.getType())) {
            BigDecimal discount = coupon.getDiscount() == null ? BigDecimal.TEN : coupon.getDiscount();
            if (discount.compareTo(BigDecimal.TEN) >= 0) {
                return BigDecimal.ZERO;
            }
            BigDecimal discountAmount = orderAmount
                    .multiply(BigDecimal.ONE.subtract(discount.divide(BigDecimal.TEN, 4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscount() != null && coupon.getMaxDiscount().compareTo(BigDecimal.ZERO) > 0
                    && discountAmount.compareTo(coupon.getMaxDiscount()) > 0) {
                discountAmount = coupon.getMaxDiscount();
            }
            return discountAmount.min(orderAmount);
        } else if ("free_shipping".equalsIgnoreCase(coupon.getType())) {
            return BigDecimal.ZERO;
        }
        throw new BusinessException("不支持的优惠券类型");
    }

    private String getDisplayValue(CouponModel coupon) {
        if ("cash".equals(coupon.getType())) {
            return String.valueOf(coupon.getValue().intValue());
        } else if ("discount".equals(coupon.getType())) {
            return String.valueOf(coupon.getDiscount().intValue());
        }
        return "";
    }

    private String getDisplayCondition(CouponModel coupon) {
        if (coupon.getMinAmount() == null || coupon.getMinAmount().compareTo(BigDecimal.ZERO) == 0) {
            return "无门槛";
        }
        return "满" + coupon.getMinAmount().intValue() + "元可用";
    }

    private String getDisplayTime(UserCouponModel userCoupon) {
        if ("USED".equals(userCoupon.getStatus()) && userCoupon.getUsedTime() != null) {
            return "已使用 " + userCoupon.getUsedTime().format(DATE_FORMATTER);
        }
        return userCoupon.getStartTime().format(DATE_FORMATTER) + " 至 " +
                userCoupon.getEndTime().format(DATE_FORMATTER);
    }

    private String getDisplayTime(CouponModel coupon) {
        return coupon.getStartTime().format(DATE_FORMATTER) + " 至 " +
                coupon.getEndTime().format(DATE_FORMATTER);
    }

    private String getColor(CouponModel coupon) {
        int hash = Math.abs(coupon.getId().hashCode());
        return COLORS[hash % COLORS.length];
    }

    private boolean isExpired(CouponModel coupon) {
        return LocalDateTime.now().isAfter(coupon.getEndTime());
    }
}