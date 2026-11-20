package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    /**
     * 查询用户的所有地址
     */
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} AND deleted = 0 ORDER BY is_default DESC, created_at DESC")
    List<UserAddress> selectByUserId(@Param("userId") String userId);

    /**
     * 查询用户的默认地址
     */
    @Select("SELECT * FROM user_address WHERE user_id = #{userId} AND is_default = TRUE AND deleted = 0")
    UserAddress selectDefaultByUserId(@Param("userId") String userId);

    /**
     * 取消用户的所有默认地址
     */
    @Update("UPDATE user_address SET is_default = FALSE, updated_at = NOW() WHERE user_id = #{userId}")
    int cancelDefault(@Param("userId") String userId);

    /**
     * 检查用户是否有地址
     */
    @Select("SELECT COUNT(*) FROM user_address WHERE user_id = #{userId} AND deleted = 0")
    int countByUserId(@Param("userId") String userId);
}
