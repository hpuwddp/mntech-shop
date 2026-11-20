package com.mntech.shop.order.service;

import com.mntech.shop.order.dto.request.CategoryQueryRequest;
import com.mntech.shop.order.dto.response.CategoryResponse;
import com.mntech.shop.order.model.CategoryModel;
import com.mntech.shop.pojo.dto.response.PageResult;

import java.util.List;

public interface ICategoryService {
    /**
     * 创建分类
     */
    CategoryModel createCategory(CategoryModel category);

    /**
     * 更新分类
     */
    CategoryModel updateCategory(CategoryModel category);

    /**
     * 删除分类（逻辑删除）
     */
    void deleteCategory(String id);

    /**
     * 根据ID查询分类
     */
    CategoryResponse getCategoryById(String id);

    /**
     * 分页查询分类
     */
    PageResult<CategoryResponse> queryCategories(CategoryQueryRequest request);

    /**
     * 查询所有分类（平铺列表）
     */
    List<CategoryResponse> getAllCategories();

    /**
     * 查询所有启用的分类
     */
    List<CategoryResponse> getActiveCategories();

    /**
     * 查询分类树（含子分类）
     */
    List<CategoryResponse> getCategoryTree();

    /**
     * 查询分类及其子分类树
     */
    List<CategoryResponse> getCategoryTreeById(String categoryId);

    /**
     * 根据父级ID查询子分类
     */
    List<CategoryResponse> getChildrenByParentId(String parentId);

    /**
     * 启用分类
     */
    void enableCategory(String id);

    /**
     * 禁用分类
     */
    void disableCategory(String id);
}
