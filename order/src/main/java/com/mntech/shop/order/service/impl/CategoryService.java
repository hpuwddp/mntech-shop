package com.mntech.shop.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mntech.shop.common.exceptions.BusinessException;
import com.mntech.shop.order.dao.CategoryMapper;
import com.mntech.shop.order.dto.request.CategoryQueryRequest;
import com.mntech.shop.order.dto.response.CategoryResponse;
import com.mntech.shop.order.model.CategoryModel;
import com.mntech.shop.order.service.ICategoryService;
import com.mntech.shop.pojo.dto.response.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService implements ICategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public CategoryModel createCategory(CategoryModel category) {
        // 检查编码是否重复
        if (StrUtil.isNotBlank(category.getCode()) && categoryMapper.countByCode(category.getCode()) > 0) {
            throw new BusinessException("分类编码已存在");
        }

        // 检查同父级下名称是否重复
        if (categoryMapper.countByParentIdAndName(category.getParentId(), category.getName()) > 0) {
            throw new BusinessException("同级分类下名称已存在");
        }

        // 设置默认值
        if (category.getStatus() == null) {
            category.setStatus("ACTIVE");
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }

        // 自动计算层级
        if (StrUtil.isBlank(category.getParentId())) {
            category.setLevel(1);
        } else {
            CategoryModel parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new BusinessException("父级分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
            if (category.getLevel() > 5) {
                throw new BusinessException("分类层级不能超过5级");
            }
        }

        category.setDeleted(0);
        categoryMapper.insert(category);
        log.info("创建分类成功: {}", category.getName());
        return category;
    }

    @Override
    public CategoryModel updateCategory(CategoryModel category) {
        CategoryModel existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查编码是否被其他分类使用
        if (StrUtil.isNotBlank(category.getCode())) {
            LambdaQueryWrapper<CategoryModel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CategoryModel::getCode, category.getCode())
                    .ne(CategoryModel::getId, category.getId());
            if (categoryMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("分类编码已被其他分类使用");
            }
            existing.setCode(category.getCode());
        }

        // 检查名称是否在兄弟节点中重复
        if (StrUtil.isNotBlank(category.getName())) {
            String parentId = category.getParentId() != null ? category.getParentId() : existing.getParentId();
            LambdaQueryWrapper<CategoryModel> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CategoryModel::getParentId, parentId)
                    .eq(CategoryModel::getName, category.getName())
                    .ne(CategoryModel::getId, category.getId());
            if (categoryMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("同级分类下名称已存在");
            }
            existing.setName(category.getName());
        }

        // 更新父级分类
        if (category.getParentId() != null) {
            // 不允许移动到自己的子分类下
            if (isChildCategory(category.getId(), category.getParentId())) {
                throw new BusinessException("不能将分类移动到自己的子分类下");
            }
            existing.setParentId(category.getParentId());
            // 重新计算层级
            if (StrUtil.isBlank(category.getParentId())) {
                existing.setLevel(1);
            } else {
                CategoryModel parent = categoryMapper.selectById(category.getParentId());
                if (parent == null) {
                    throw new BusinessException("父级分类不存在");
                }
                existing.setLevel(parent.getLevel() + 1);
            }
        }

        // 更新其他字段
        if (category.getSortOrder() != null) {
            existing.setSortOrder(category.getSortOrder());
        }
        if (category.getIcon() != null) {
            existing.setIcon(category.getIcon());
        }
        if (category.getImage() != null) {
            existing.setImage(category.getImage());
        }
        if (category.getDescription() != null) {
            existing.setDescription(category.getDescription());
        }
        if (category.getStatus() != null) {
            existing.setStatus(category.getStatus());
        }

        existing.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(existing);
        log.info("更新分类成功: {}", existing.getName());
        return existing;
    }

    @Override
    public void deleteCategory(String id) {
        CategoryModel category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有子分类
        List<CategoryModel> children = categoryMapper.selectByParentId(id);
        if (CollectionUtil.isNotEmpty(children)) {
            throw new BusinessException("该分类下存在子分类，不能删除");
        }

        categoryMapper.deleteById(id);
        log.info("删除分类成功: {}", category.getName());
    }

    @Override
    public CategoryResponse getCategoryById(String id) {
        CategoryModel category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return convertToResponse(category);
    }

    @Override
    public PageResult<CategoryResponse> queryCategories(CategoryQueryRequest request) {
        LambdaQueryWrapper<CategoryModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryModel::getDeleted, 0);

        if (StrUtil.isNotBlank(request.getName())) {
            wrapper.like(CategoryModel::getName, request.getName());
        }
        if (StrUtil.isNotBlank(request.getCode())) {
            wrapper.eq(CategoryModel::getCode, request.getCode());
        }

        if (StrUtil.isNotBlank(request.getStatus())) {
            wrapper.eq(CategoryModel::getStatus, request.getStatus());
        }

        wrapper.orderByAsc(CategoryModel::getSortOrder)
                .orderByDesc(CategoryModel::getCreatedAt);

        Page<CategoryModel> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<CategoryModel> categoryPage = categoryMapper.selectPage(page, wrapper);

        List<CategoryResponse> list = categoryPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(categoryPage.getTotal(), request.getPageNum(), request.getPageSize(), list);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        LambdaQueryWrapper<CategoryModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CategoryModel::getDeleted, 0)
                .orderByAsc(CategoryModel::getSortOrder)
                .orderByDesc(CategoryModel::getCreatedAt);
        List<CategoryModel> categories = categoryMapper.selectList(wrapper);
        return categories.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getActiveCategories() {
        List<CategoryModel> categories = categoryMapper.selectActiveCategories();
        return categories.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getCategoryTree() {
        List<CategoryModel> rootCategories = categoryMapper.selectRootCategories();
        List<CategoryResponse> result = new ArrayList<>();
        for (CategoryModel root : rootCategories) {
            CategoryResponse response = convertToResponse(root);
            response.setChildren(buildCategoryTree(root.getId()));
            result.add(response);
        }
        return result;
    }

    @Override
    public List<CategoryResponse> getCategoryTreeById(String categoryId) {
        List<CategoryModel> categories = categoryMapper.selectCategoryTree(categoryId);
        if (CollectionUtil.isEmpty(categories)) {
            return new ArrayList<>();
        }
        CategoryModel root = categories.get(0);
        CategoryResponse response = convertToResponse(root);
        response.setChildren(buildCategoryTree(root.getId()));
        List<CategoryResponse> result = new ArrayList<>();
        result.add(response);
        return result;
    }

    @Override
    public List<CategoryResponse> getChildrenByParentId(String parentId) {
        List<CategoryModel> children = categoryMapper.selectByParentId(parentId);
        return children.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public void enableCategory(String id) {
        CategoryModel category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        category.setStatus("ACTIVE");
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
        log.info("启用分类成功: {}", category.getName());
    }

    @Override
    public void disableCategory(String id) {
        CategoryModel category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        category.setStatus("INACTIVE");
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.updateById(category);
        log.info("禁用分类成功: {}", category.getName());
    }

    // ===== 私有方法 =====

    /**
     * 递归构建分类树
     */
    private List<CategoryResponse> buildCategoryTree(String parentId) {
        List<CategoryModel> children = categoryMapper.selectByParentId(parentId);
        List<CategoryResponse> result = new ArrayList<>();
        for (CategoryModel child : children) {
            CategoryResponse response = convertToResponse(child);
            response.setChildren(buildCategoryTree(child.getId()));
            result.add(response);
        }
        return result;
    }

    /**
     * 检查targetId是否是sourceId的子分类
     */
    private boolean isChildCategory(String sourceId, String targetId) {
        if (StrUtil.isBlank(targetId)) {
            return false;
        }
        CategoryModel target = categoryMapper.selectById(targetId);
        if (target == null) {
            return false;
        }
        String parentId = target.getParentId();
        while (StrUtil.isNotBlank(parentId)) {
            if (parentId.equals(sourceId)) {
                return true;
            }
            CategoryModel parent = categoryMapper.selectById(parentId);
            if (parent == null) {
                break;
            }
            parentId = parent.getParentId();
        }
        return false;
    }

    /**
     * 转换为响应对象
     */
    private CategoryResponse convertToResponse(CategoryModel category) {
        CategoryResponse response = new CategoryResponse();
        BeanUtil.copyProperties(category, response);

        // 状态描述
        if ("ACTIVE".equals(category.getStatus())) {
            response.setStatusDesc("启用");
        } else if ("INACTIVE".equals(category.getStatus())) {
            response.setStatusDesc("禁用");
        }

        // 查询父级分类名称
        if (StrUtil.isNotBlank(category.getParentId())) {
            CategoryModel parent = categoryMapper.selectById(category.getParentId());
            if (parent != null) {
                response.setParentName(parent.getName());
            }
        }

        return response;
    }
}
