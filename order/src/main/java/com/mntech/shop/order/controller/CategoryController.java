package com.mntech.shop.order.controller;

import com.mntech.shop.order.dto.request.CategoryQueryRequest;
import com.mntech.shop.order.dto.response.CategoryResponse;
import com.mntech.shop.order.model.CategoryModel;
import com.mntech.shop.order.service.ICategoryService;
import com.mntech.shop.pojo.dto.response.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 分类管理Controller
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {
    private final ICategoryService categoryService;

    // ==================== 查询接口 ====================

    /**
     * 查询分类树（所有分类的树形结构）
     */
    @GetMapping("/tree")
    public ResponseEntity<List<CategoryResponse>> getCategoryTree() {
        List<CategoryResponse> tree = categoryService.getCategoryTree();
        return ResponseEntity.ok(tree);
    }

    /**
     * 查询指定分类及其子分类树
     */
    @GetMapping("/{id}/tree")
    public ResponseEntity<List<CategoryResponse>> getCategoryTreeById(@PathVariable String id) {
        List<CategoryResponse> tree = categoryService.getCategoryTreeById(id);
        return ResponseEntity.ok(tree);
    }

    /**
     * 分页查询分类
     */
    @PostMapping("/query")
    public ResponseEntity<PageResult<CategoryResponse>> queryCategories(
            @RequestBody CategoryQueryRequest request) {
        PageResult<CategoryResponse> result = categoryService.queryCategories(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询所有分类（平铺列表）
     */
    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 查询所有启用的分类
     */
    @GetMapping("/active")
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        List<CategoryResponse> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 查询一级分类
     */
    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        CategoryQueryRequest request = new CategoryQueryRequest();
        request.setPageNum(1);
        request.setPageSize(100);
        // parentId 为 null 查询一级分类
        PageResult<CategoryResponse> result = categoryService.queryCategories(request);
        return ResponseEntity.ok(result.getList());
    }

    /**
     * 根据父级ID查询子分类
     */
    @GetMapping("/parent/{parentId}/children")
    public ResponseEntity<List<CategoryResponse>> getChildrenByParentId(@PathVariable String parentId) {
        List<CategoryResponse> children = categoryService.getChildrenByParentId(parentId);
        return ResponseEntity.ok(children);
    }

    /**
     * 查询分类详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable String id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    // ==================== 管理接口 ====================

    /**
     * 创建分类
     */
    @PostMapping
    public ResponseEntity<CategoryModel> createCategory(@Valid @RequestBody CategoryModel category) {
        CategoryModel result = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryModel> updateCategory(@PathVariable String id,
                                                        @Valid @RequestBody CategoryModel category) {
        category.setId(id);
        CategoryModel result = categoryService.updateCategory(category);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 启用分类
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<Void> enableCategory(@PathVariable String id) {
        categoryService.enableCategory(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 禁用分类
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<Void> disableCategory(@PathVariable String id) {
        categoryService.disableCategory(id);
        return ResponseEntity.ok().build();
    }
}
