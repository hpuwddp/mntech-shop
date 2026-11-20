package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.DistributionRelationModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DistributionRelationMapper extends BaseMapper<DistributionRelationModel> {

    /**
     * 查询用户当前上级关系（每个用户最多一条）
     */
    @Select("SELECT * FROM distribution_relation WHERE user_id = #{userId} AND deleted = 0")
    DistributionRelationModel selectByUserId(@Param("userId") String userId);
}