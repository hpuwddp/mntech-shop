package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.BrandModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BrandMapper extends BaseMapper<BrandModel> {
    /**
     * 查询所有启用的品牌
     */
    @Select("SELECT * FROM brand WHERE status = 'ACTIVE' AND deleted = 0 ORDER BY sort_order ASC, created_at DESC")
    List<BrandModel> selectActiveBrands();

    /**
     * 根据名称模糊查询品牌
     */
    @Select("SELECT * FROM brand WHERE name LIKE CONCAT('%', #{name}, '%') AND deleted = 0 ORDER BY sort_order ASC")
    List<BrandModel> selectByNameLike(@Param("name") String name);

    /**
     * 检查品牌名称是否存在
     */
    @Select("SELECT COUNT(*) FROM brand WHERE name = #{name} AND deleted = 0")
    int countByName(@Param("name") String name);

    /**
     * 检查品牌编码是否存在
     */
    @Select("SELECT COUNT(*) FROM brand WHERE code = #{code} AND deleted = 0")
    int countByCode(@Param("code") String code);
}
