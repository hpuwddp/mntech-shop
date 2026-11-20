package com.mntech.shop.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.dao.BrandMapper;
import com.mntech.shop.order.dao.CategoryMapper;
import com.mntech.shop.order.dao.ProductMapper;
import com.mntech.shop.order.dao.SkuMapper;
import com.mntech.shop.order.dto.request.ProductCreateRequest;
import com.mntech.shop.order.dto.request.ProductQueryRequest;
import com.mntech.shop.order.dto.request.ProductUpdateRequest;
import com.mntech.shop.order.dto.request.SkuCreateRequest;
import com.mntech.shop.order.dto.response.ProductResponse;
import com.mntech.shop.order.dto.response.SkuResponse;
import com.mntech.shop.common.enums.AuditStatus;
import com.mntech.shop.common.enums.ProductStatus;
import com.mntech.shop.common.enums.SkuStatus;
import com.mntech.shop.order.model.BrandModel;
import com.mntech.shop.order.model.CategoryModel;
import com.mntech.shop.order.service.IProductService;
import com.mntech.shop.order.util.PgArrayUtil;
import com.mntech.shop.pojo.dto.response.PageResult;
import com.mntech.shop.pojo.model.ProductModel;
import com.mntech.shop.pojo.model.SkuModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 产品服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProductService implements IProductService {  // ✅ 修正类名

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final BrandMapper brandMapper;
    private final CategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        try {
            // 1. 自动生成 SPU 编码
            String spuCode = generateSpuCode();

            // 2. 检查SPU编码是否重复
            LambdaQueryWrapper<ProductModel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductModel::getSpuCode, spuCode);
            if (productMapper.selectCount(wrapper) > 0) {
                spuCode = generateSpuCode();
            }

            // 3. 构建产品实体
            ProductModel product = new ProductModel();
            product.setSpuCode(spuCode);
            product.setName(request.getName());
            product.setSubtitle(request.getSubtitle());
            product.setDescription(request.getDescription());
            product.setBrandId(request.getBrandId());
            product.setCategoryId(request.getCategoryId());
            product.setOriginalPrice(request.getOriginalPrice());
            product.setSellingPrice(request.getSellingPrice());
            product.setMemberPrice(request.getMemberPrice());
            product.setTotalStock(request.getTotalStock() != null ? request.getTotalStock() : 0);
            product.setAvailableStock(request.getTotalStock() != null ? request.getTotalStock() : 0);
            product.setLockedStock(0);
            product.setSoldStock(0);
            product.setStatus(ProductStatus.DRAFT.name());
            product.setAuditStatus(AuditStatus.PENDING.name());
            product.setHasSku(request.getHasSku() != null && request.getHasSku());
            product.setWeight(request.getWeight());
            //   product.setShippingTemplateId(request.getShippingTemplateId());
            product.setDeliveryTime(request.getDeliveryTime());
            product.setMainImage(request.getMainImage());

            // ✅ 使用 ObjectMapper 替代 JSONUtil
            if (CollectionUtil.isNotEmpty(request.getImages())) {
                product.setImages(objectMapper.writeValueAsString(request.getImages()));
            }
            if (CollectionUtil.isNotEmpty(request.getTags())) {
                product.setTags(objectMapper.writeValueAsString(request.getTags()));
            }
            if (request.getSkuTemplate() != null) {
                product.setSkuTemplate(objectMapper.writeValueAsString(request.getSkuTemplate()));
            }

            product.setSalesCount(0);
            product.setViewCount(0);
            product.setFavoriteCount(0);
            product.setReviewCount(0);
            product.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "system");
            product.setVersion(0);
            product.setDeleted(0);

            productMapper.insert(product);

            return getProduct(product.getId());
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            throw new BusinessException("数据格式错误");
        }
    }

    /**
     * 生成SPU编码
     * 格式：SPU-20240623-0001
     */
    private String generateSpuCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<ProductModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(ProductModel::getSpuCode, "SPU-" + date);
        wrapper.orderByDesc(ProductModel::getSpuCode);
        wrapper.last("LIMIT 1");
        ProductModel lastProduct = productMapper.selectOne(wrapper);

        int seq = 1;
        if (lastProduct != null && lastProduct.getSpuCode() != null) {
            String[] parts = lastProduct.getSpuCode().split("-");
            if (parts.length == 3) {
                try {
                    seq = Integer.parseInt(parts[2]) + 1;
                } catch (NumberFormatException e) {
                    seq = 1;
                }
            }
        }
        return String.format("SPU-%s-%04d", date, seq);
    }

    @Override
    public ProductResponse updateProduct(String id, ProductUpdateRequest request) {
        try {
            ProductModel product = productMapper.selectById(id);
            if (product == null) {
                throw new BusinessException("产品不存在");
            }

            if (StrUtil.isNotBlank(request.getName())) {
                product.setName(request.getName());
            }
            if (request.getSubtitle() != null) {
                product.setSubtitle(request.getSubtitle());
            }
            if (request.getDescription() != null) {
                product.setDescription(request.getDescription());
            }
            if (StrUtil.isNotBlank(request.getBrandId())) {
                product.setBrandId(request.getBrandId());
            }
            if (StrUtil.isNotBlank(request.getCategoryId())) {
                product.setCategoryId(request.getCategoryId());
            }
            if (request.getOriginalPrice() != null) {
                product.setOriginalPrice(request.getOriginalPrice());
            }
            if (request.getSellingPrice() != null) {
                product.setSellingPrice(request.getSellingPrice());
            }
            if (request.getMemberPrice() != null) {
                product.setMemberPrice(request.getMemberPrice());
            }
            if (request.getPromotionPrice() != null) {
                product.setPromotionPrice(request.getPromotionPrice());
                product.setPromotionStart(request.getPromotionStart());
                product.setPromotionEnd(request.getPromotionEnd());
            }
            if (request.getWeight() != null) {
                product.setWeight(request.getWeight());
            }
            if (request.getVolume() != null) {
                product.setVolume(request.getVolume());
            }
//            if (request.getShippingTemplateId() != null) {
//                product.setShippingTemplateId(request.getShippingTemplateId());
//            }
            if (request.getDeliveryTime() != null) {
                product.setDeliveryTime(request.getDeliveryTime());
            }
            if (request.getMainImage() != null) {
                product.setMainImage(request.getMainImage());
            }
            if (request.getImages() != null) {
                product.setImages(objectMapper.writeValueAsString(request.getImages()));
            }
            if (request.getTags() != null) {
                product.setTags(objectMapper.writeValueAsString(request.getTags()));
            }

            product.setUpdatedAt(LocalDateTime.now());
            product.setVersion(product.getVersion() + 1);
            productMapper.updateById(product);

            return getProduct(id);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            throw new BusinessException("数据格式错误");
        }
    }

    @Override
    public void deleteProduct(String id, String operatorId) {
        ProductModel product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (ProductStatus.ON_SHELF.name().equals(product.getStatus())) {
            throw new BusinessException("已上架的产品不能删除，请先下架");
        }
        product.setStatus(ProductStatus.DELETED.name());
        product.setUpdatedBy(operatorId);
        product.setUpdatedAt(LocalDateTime.now());
        product.setVersion(product.getVersion() + 1);
        productMapper.updateById(product);
    }

    @Override
    public ProductResponse getProduct(String id) {
        try {
            ProductModel product = productMapper.selectById(id);
            if (product == null) {
                throw new BusinessException("产品不存在");
            }
            return convertToResponse(product);
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败", e);
            throw new BusinessException("数据格式错误");
        }
    }

    @Override
    public PageResult<ProductResponse> queryProducts(ProductQueryRequest request) {
        try {
            LambdaQueryWrapper<ProductModel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductModel::getDeleted, 0);

            if (StrUtil.isNotBlank(request.getName())) {
                wrapper.like(ProductModel::getName, request.getName());
            }
            if (StrUtil.isNotBlank(request.getBrandId())) {
                wrapper.eq(ProductModel::getBrandId, request.getBrandId());
            }
if (StrUtil.isNotBlank(request.getCategoryId())) {
                String categoryId = request.getCategoryId().trim();
                if (categoryId.contains(",")) {
                    List<String> categoryIdList = new ArrayList<>();
                    for (String cid : categoryId.split(",")) {
                        if (StrUtil.isNotBlank(cid)) {
                            categoryIdList.add(cid.trim());
                        }
                    }
                    if (!categoryIdList.isEmpty()) {
                        wrapper.in(ProductModel::getCategoryId, categoryIdList);
                    }
                } else {
                    wrapper.eq(ProductModel::getCategoryId, categoryId);
                }
            }
            if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
                List<String> productIdList = new ArrayList<>();
                for (String pid : request.getProductIds()) {
                    if (StrUtil.isNotBlank(pid)) {
                        productIdList.add(pid.trim());
                    }
                }
                if (!productIdList.isEmpty()) {
                    wrapper.in(ProductModel::getId, productIdList);
                }
            }
            if (StrUtil.isNotBlank(request.getStatus())) {
                wrapper.eq(ProductModel::getStatus, request.getStatus());
            }
            if (request.getMinPrice() != null) {
                wrapper.ge(ProductModel::getSellingPrice, request.getMinPrice());
            }
            if (request.getMaxPrice() != null) {
                wrapper.le(ProductModel::getSellingPrice, request.getMaxPrice());
            }
if (request.getOnShelf() != null && request.getOnShelf()) {
                wrapper.eq(ProductModel::getStatus, ProductStatus.ON_SHELF.name());
            }

            if (StrUtil.isNotBlank(request.getOrderBy())) {
                boolean isAsc = "asc".equalsIgnoreCase(request.getOrderDirection());
                switch (request.getOrderBy()) {
                    case "createdAt":
                        wrapper.orderBy(true, isAsc, ProductModel::getCreatedAt);
                        break;
                    case "sellingPrice":
                        wrapper.orderBy(true, isAsc, ProductModel::getSellingPrice);
                        break;
                    case "salesCount":
                        wrapper.orderBy(true, isAsc, ProductModel::getSalesCount);
                        break;
                    default:
                        wrapper.orderBy(true, false, ProductModel::getCreatedAt);
                }
            } else {
                wrapper.orderBy(true, false, ProductModel::getCreatedAt);
            }

            Page<ProductModel> page = new Page<>(request.getPageNum(), request.getPageSize());
            Page<ProductModel> productPage = productMapper.selectPage(page, wrapper);

            List<ProductResponse> list = new ArrayList<>();
            for (ProductModel product : productPage.getRecords()) {
                list.add(convertToResponse(product));
            }

            return PageResult.of(productPage.getTotal(), request.getPageNum(), request.getPageSize(), list);
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败", e);
            throw new BusinessException("数据格式错误");
        }
    }

    @Override
    public List<ProductResponse> getOnShelfProducts() {
        try {
            List<ProductModel> products = productMapper.selectOnShelfProducts();
            List<ProductResponse> result = new ArrayList<>();
            for (ProductModel product : products) {
                result.add(convertToResponse(product));
            }
            return result;
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败", e);
            throw new BusinessException("数据格式错误");
        }
    }

    @Override
    public List<ProductResponse> getPromotionProducts() {
        try {
            List<ProductModel> products = productMapper.selectPromotionProducts();
            List<ProductResponse> result = new ArrayList<>();
            for (ProductModel product : products) {
                result.add(convertToResponse(product));
            }
            return result;
        } catch (JsonProcessingException e) {
            log.error("JSON反序列化失败", e);
            throw new BusinessException("数据格式错误");
        }
    }

    @Override
    public ProductResponse submitForAudit(String id, String operatorId) {
        ProductModel product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (!ProductStatus.DRAFT.name().equals(product.getStatus())
                && !ProductStatus.REJECTED.name().equals(product.getStatus())) {
            throw new BusinessException("只有草稿或已驳回状态才能提交审核");
        }
        product.setStatus(ProductStatus.PENDING.name());
        product.setAuditStatus(AuditStatus.PENDING.name());
        product.setUpdatedBy(operatorId);
        product.setUpdatedAt(LocalDateTime.now());
        product.setVersion(product.getVersion() + 1);
        productMapper.updateById(product);
        return getProduct(id);
    }

    @Override
    public ProductResponse approveProduct(String id, String operatorId, String remark) {
        ProductModel product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (!ProductStatus.PENDING.name().equals(product.getStatus())) {
            throw new BusinessException("只有待审核状态才能通过");
        }
        product.setStatus(ProductStatus.APPROVED.name());
        product.setAuditStatus(AuditStatus.APPROVED.name());
        product.setAuditorId(operatorId);
        product.setAuditTime(LocalDateTime.now());
        product.setAuditRemark(remark);
        product.setUpdatedBy(operatorId);
        product.setUpdatedAt(LocalDateTime.now());
        product.setVersion(product.getVersion() + 1);
        productMapper.updateById(product);
        return getProduct(id);
    }

    @Override
    public ProductResponse rejectProduct(String id, String operatorId, String remark) {
        ProductModel product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (!ProductStatus.PENDING.name().equals(product.getStatus())) {
            throw new BusinessException("只有待审核状态才能驳回");
        }
        product.setStatus(ProductStatus.REJECTED.name());
        product.setAuditStatus(AuditStatus.REJECTED.name());
        product.setAuditorId(operatorId);
        product.setAuditTime(LocalDateTime.now());
        product.setAuditRemark(remark);
        product.setUpdatedBy(operatorId);
        product.setUpdatedAt(LocalDateTime.now());
        product.setVersion(product.getVersion() + 1);
        productMapper.updateById(product);
        return getProduct(id);
    }

    @Override
    public ProductResponse putOnShelf(String id, String operatorId) {
        ProductModel product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (!ProductStatus.APPROVED.name().equals(product.getStatus())
                && !ProductStatus.OFF_SHELF.name().equals(product.getStatus())) {
            throw new BusinessException("只有已审核或已下架状态才能上架");
        }
        if (product.getAvailableStock() == null || product.getAvailableStock() <= 0) {
            throw new BusinessException("库存不足，无法上架");
        }
        product.setStatus(ProductStatus.ON_SHELF.name());
        product.setUpdatedBy(operatorId);
        product.setUpdatedAt(LocalDateTime.now());
        product.setVersion(product.getVersion() + 1);
        productMapper.updateById(product);
        return getProduct(id);
    }

    @Override
    public ProductResponse putOffShelf(String id, String operatorId) {
        ProductModel product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (!ProductStatus.ON_SHELF.name().equals(product.getStatus())) {
            throw new BusinessException("只有已上架状态才能下架");
        }
        product.setStatus(ProductStatus.OFF_SHELF.name());
        product.setUpdatedBy(operatorId);
        product.setUpdatedAt(LocalDateTime.now());
        product.setVersion(product.getVersion() + 1);
        productMapper.updateById(product);
        return getProduct(id);
    }

    @Override
    public ProductResponse reduceStock(String productId, Integer quantity) {
        ProductModel product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (product.getHasSku()) {
            throw new BusinessException("多规格产品请使用SKU级别库存操作");
        }
        int result = productMapper.reduceStock(productId, quantity);
        if (result == 0) {
            throw new BusinessException("库存扣减失败，可能库存不足");
        }
        return getProduct(productId);
    }

    @Override
    public ProductResponse confirmReduceStock(String productId, Integer quantity) {
        ProductModel product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (product.getHasSku()) {
            throw new BusinessException("多规格产品请使用SKU级别库存操作");
        }
        if (product.getLockedStock() < quantity) {
            throw new BusinessException("锁定库存不足");
        }
        product.setLockedStock(product.getLockedStock() - quantity);
        product.setSoldStock(product.getSoldStock() + quantity);
        product.setSalesCount(product.getSalesCount() + quantity);
        product.setUpdatedAt(LocalDateTime.now());
        product.setVersion(product.getVersion() + 1);
        productMapper.updateById(product);
        return getProduct(productId);
    }

    @Override
    public ProductResponse releaseLockedStock(String productId, Integer quantity) {
        ProductModel product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (product.getHasSku()) {
            throw new BusinessException("多规格产品请使用SKU级别库存操作");
        }
        if (product.getLockedStock() < quantity) {
            throw new BusinessException("锁定库存不足");
        }
        product.setLockedStock(product.getLockedStock() - quantity);
        product.setAvailableStock(product.getAvailableStock() + quantity);
        product.setUpdatedAt(LocalDateTime.now());
        product.setVersion(product.getVersion() + 1);
        productMapper.updateById(product);
        return getProduct(productId);
    }

    @Override
    public ProductResponse increaseStock(String productId, Integer quantity) {
        ProductModel product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("产品不存在");
        }
        if (product.getHasSku()) {
            throw new BusinessException("多规格产品请使用SKU级别库存操作");
        }
        int result = productMapper.increaseStock(productId, quantity);
        if (result == 0) {
            throw new BusinessException("库存增加失败");
        }
        return getProduct(productId);
    }

    @Override
    public SkuResponse addSku(SkuCreateRequest request) {
        try {
            ProductModel product = productMapper.selectById(request.getProductId());
            if (product == null) {
                throw new BusinessException("产品不存在");
            }
            if (!product.getHasSku()) {
                throw new BusinessException("当前产品不是多规格产品");
            }

            // 检查SKU编码是否重复
            LambdaQueryWrapper<SkuModel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SkuModel::getSkuCode, request.getSkuCode());
            if (skuMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("SKU编码已存在");
            }

            // 检查属性组合是否重复
            String attributeHash = generateAttributeHash(request.getAttributes());
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SkuModel::getProductId, request.getProductId())
                    .eq(SkuModel::getAttributeHash, attributeHash);
            if (skuMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("SKU属性组合已存在");
            }

            SkuModel sku = new SkuModel();
            sku.setProductId(request.getProductId());
            sku.setSkuCode(request.getSkuCode());
            sku.setSkuName(request.getSkuName());
            sku.setAttributes(objectMapper.writeValueAsString(request.getAttributes()));
            sku.setAttributeHash(attributeHash);
            sku.setSellingPrice(request.getSellingPrice());
            sku.setTotalStock(request.getTotalStock());
            sku.setAvailableStock(request.getTotalStock());
            sku.setLockedStock(0);
            sku.setSoldStock(0);
            sku.setStatus(SkuStatus.ACTIVE.name());
            sku.setSalesCount(0);
            sku.setVersion(0);
            sku.setDeleted(0);

            skuMapper.insert(sku);

            // 更新产品的SKU模板
            updateProductSkuTemplate(product);

            return convertSkuToResponse(sku);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            throw new BusinessException("数据格式错误");
        }
    }

    @Override
    public void deleteSku(String productId, String skuId) {
        SkuModel sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }
        if (!productId.equals(sku.getProductId())) {
            throw new BusinessException("SKU不属于该产品");
        }
        sku.setStatus(SkuStatus.DELETED.name());
        sku.setUpdatedAt(LocalDateTime.now());
        sku.setVersion(sku.getVersion() + 1);
        skuMapper.updateById(sku);

        ProductModel product = productMapper.selectById(productId);
        if (product != null) {
            updateProductSkuTemplate(product);
        }
    }

    @Override
    public PageResult<ProductResponse> getProductsByCategory(ProductQueryRequest request) {
        try {
            // 1. 验证分类是否存在
            if (StrUtil.isBlank(request.getCategoryId())) {
                throw new BusinessException("分类ID不能为空");
            }

            CategoryModel category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                throw new BusinessException("分类不存在");
            }

            // 2. 构建查询条件
            LambdaQueryWrapper<ProductModel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductModel::getDeleted, 0);


            // 只查当前分类
            wrapper.eq(ProductModel::getCategoryId, request.getCategoryId());


            // 4. 只查询上架产品

            wrapper.in(ProductModel::getStatus,
                    Arrays.asList(ProductStatus.ON_SHELF.name(),
                            ProductStatus.OFF_SHELF.name(),
                            ProductStatus.APPROVED.name()));


            // 6. 品牌过滤
            if (StrUtil.isNotBlank(request.getBrandId())) {
                wrapper.eq(ProductModel::getBrandId, request.getBrandId());
            }

            // 7. 价格范围过滤
            if (request.getMinPrice() != null) {
                wrapper.ge(ProductModel::getSellingPrice, request.getMinPrice());
            }
            if (request.getMaxPrice() != null) {
                wrapper.le(ProductModel::getSellingPrice, request.getMaxPrice());
            }

            // 8. 排序
            String orderBy = request.getOrderBy() != null ? request.getOrderBy() : "salesCount";
            String orderDirection = request.getOrderDirection() != null ? request.getOrderDirection() : "desc";
            boolean isAsc = "asc".equalsIgnoreCase(orderDirection);

            switch (orderBy) {
                case "salesCount":
                    wrapper.orderBy(true, isAsc, ProductModel::getSalesCount);
                    break;
                case "sellingPrice":
                    wrapper.orderBy(true, isAsc, ProductModel::getSellingPrice);
                    break;
                case "createdAt":
                    wrapper.orderBy(true, isAsc, ProductModel::getCreatedAt);
                    break;
                case "rating":
                    wrapper.orderBy(true, isAsc, ProductModel::getRating);
                    break;
                default:
                    wrapper.orderBy(true, false, ProductModel::getSalesCount);
            }

            // 9. 分页查询
            Page<ProductModel> page = new Page<>(request.getPageNum(), request.getPageSize());
            Page<ProductModel> productPage = productMapper.selectPage(page, wrapper);

            // 10. 转换为响应对象
            List<ProductResponse> list = new ArrayList<>();
            for (ProductModel product : productPage.getRecords()) {
                list.add(convertToResponse(product));
            }

            return PageResult.of(
                    productPage.getTotal(),
                    request.getPageNum(),
                    request.getPageSize(),
                    list
            );

        } catch (Exception e) {
            log.error("根据分类查询产品失败", e);
            throw new BusinessException("查询产品失败: " + e.getMessage());
        }
    }

    /**
     * 获取分类及其所有子分类ID
     */
    private List<String> getCategoryAndChildrenIds(String categoryId) {
        List<String> ids = new ArrayList<>();
        ids.add(categoryId);

        // 递归查询所有子分类
        List<CategoryModel> children = categoryMapper.selectByParentId(categoryId);
        if (CollectionUtil.isNotEmpty(children)) {
            for (CategoryModel child : children) {
                ids.addAll(getCategoryAndChildrenIds(child.getId()));
            }
        }

        return ids;
    }

    // ===== 私有转换方法 =====

    private ProductResponse convertToResponse(ProductModel product) throws JsonProcessingException {
        ProductResponse response = new ProductResponse();
        BeanUtil.copyProperties(product, response, "images", "tags", "skuTemplate");

        // 状态描述
        try {
            ProductStatus status = ProductStatus.valueOf(product.getStatus());
            response.setStatusDesc(status.getDescription());
        } catch (IllegalArgumentException e) {
            response.setStatusDesc(product.getStatus());
        }

        // 审核状态描述
        if (product.getAuditStatus() != null) {
            try {
                AuditStatus auditStatus = AuditStatus.valueOf(product.getAuditStatus());
                response.setAuditStatusDesc(auditStatus.getDescription());
            } catch (IllegalArgumentException e) {
                response.setAuditStatusDesc(product.getAuditStatus());
            }
        }

        // 当前价格
        BigDecimal currentPrice = product.getSellingPrice();
        if (product.getPromotionPrice() != null
                && product.getPromotionStart() != null
                && product.getPromotionEnd() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(product.getPromotionStart()) && now.isBefore(product.getPromotionEnd())) {
                currentPrice = product.getPromotionPrice();
            }
        }
        response.setCurrentPrice(currentPrice);

        // ✅ 使用 ObjectMapper 解析 JSON 字段
        if (StrUtil.isNotBlank(product.getImages())) {
            response.setImages(PgArrayUtil.parseToList(product.getImages()));
        }
        if (StrUtil.isNotBlank(product.getTags())) {
            response.setTags(PgArrayUtil.parseToList(product.getTags()));
        }
        if (StrUtil.isNotBlank(product.getSkuTemplate())) {
            response.setSkuTemplate(PgArrayUtil.parseToMap(product.getSkuTemplate()));
        }

        // 查询品牌名称
        if (StrUtil.isNotBlank(product.getBrandId())) {
            BrandModel brand = brandMapper.selectById(product.getBrandId());
            if (brand != null) {
                response.setBrandName(brand.getName());
            }
        }

        // 查询分类名称
        if (StrUtil.isNotBlank(product.getCategoryId())) {
            CategoryModel category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getName());
            }
        }

        // 查询SKU列表
        List<SkuModel> skus = skuMapper.selectByProductId(product.getId());
        if (CollectionUtil.isNotEmpty(skus)) {
            List<SkuResponse> skuResponses = new ArrayList<>();
            for (SkuModel sku : skus) {
                if (!SkuStatus.DELETED.name().equals(sku.getStatus())) {
                    skuResponses.add(convertSkuToResponse(sku));
                }
            }
            response.setSkus(skuResponses);
        }

        return response;
    }

    private SkuResponse convertSkuToResponse(SkuModel sku) throws JsonProcessingException {
        SkuResponse response = new SkuResponse();
        BeanUtil.copyProperties(sku, response, "attributes");

        // 状态描述
        try {
            SkuStatus status = SkuStatus.valueOf(sku.getStatus());
            response.setStatusDesc(status.getDescription());
        } catch (IllegalArgumentException e) {
            response.setStatusDesc(sku.getStatus());
        }

        // ✅ 使用 ObjectMapper 解析属性JSON
        if (StrUtil.isNotBlank(sku.getAttributes())) {
            response.setAttributes(PgArrayUtil.parseToMap(sku.getAttributes()));
        }

        // 当前价格
        BigDecimal currentPrice = sku.getSellingPrice();
        if (sku.getPromotionPrice() != null
                && sku.getPromotionStart() != null
                && sku.getPromotionEnd() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(sku.getPromotionStart()) && now.isBefore(sku.getPromotionEnd())) {
                currentPrice = sku.getPromotionPrice();
            }
        }
        response.setCurrentPrice(currentPrice);

        return response;
    }

    private String generateAttributeHash(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        attributes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> sb.append(e.getKey()).append(":").append(e.getValue()).append(";"));
        return String.valueOf(sb.toString().hashCode());
    }

    /**
     * 更新产品SKU模板（完全使用 ObjectMapper）
     */
    private void updateProductSkuTemplate(ProductModel product) {
        try {
            List<SkuModel> skus = skuMapper.selectByProductId(product.getId());
            if (CollectionUtil.isEmpty(skus)) {
                product.setSkuTemplate(null);
                productMapper.updateById(product);
                return;
            }

            Map<String, List<String>> template = new HashMap<>();

            for (SkuModel sku : skus) {
                if (!SkuStatus.ACTIVE.name().equals(sku.getStatus())) {
                    continue;
                }

                String attributesJson = sku.getAttributes();
                if (StrUtil.isBlank(attributesJson)) {
                    continue;
                }

                try {
                    // ✅ 使用 ObjectMapper 解析 JSON
                    Map<String, String> attributes = objectMapper.readValue(attributesJson,
                            new TypeReference<Map<String, String>>() {
                            });

                    if (attributes == null || attributes.isEmpty()) {
                        continue;
                    }

                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        if (StrUtil.isNotBlank(key) && StrUtil.isNotBlank(value)) {
                            if (!template.containsKey(key)) {
                                template.put(key, new ArrayList<>());
                            }
                            if (!template.get(key).contains(value)) {
                                template.get(key).add(value);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析SKU属性JSON失败: skuId={}", sku.getId(), e);
                }
            }

            // ✅ 使用 ObjectMapper 序列化
            product.setSkuTemplate(template.isEmpty() ? null : objectMapper.writeValueAsString(template));
            productMapper.updateById(product);
        } catch (Exception e) {
            log.error("更新产品SKU模板失败: productId={}", product.getId(), e);
            product.setSkuTemplate(null);
            productMapper.updateById(product);
        }
    }
}