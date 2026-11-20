package com.mntech.shop.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mntech.shop.order.model.UserModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper extends BaseMapper<UserModel> {

    @Select("SELECT * FROM users WHERE openid = #{openid} AND deleted = 0")
    UserModel selectByOpenid(@Param("openid") String openid);

    @Select("SELECT * FROM users WHERE token = #{token} AND deleted = 0")
    UserModel selectByToken(@Param("token") String token);

    @Select("SELECT * FROM users WHERE id = #{userId} AND deleted = 0")
    UserModel selectByUserId(@Param("userId") String userId);

    @Update("UPDATE users SET token = #{token}, token_expire_time = #{expireTime}, last_login_time = NOW(), updated_at = NOW() WHERE id = #{userId}")
    int updateToken(@Param("userId") String userId, @Param("token") String token, @Param("expireTime") LocalDateTime expireTime);

    @Update("UPDATE users SET token = NULL, token_expire_time = NULL, updated_at = NOW() WHERE id = #{userId}")
    int clearToken(@Param("userId") String userId);


    @Update("UPDATE users SET nickname = #{nickname}, updated_at = NOW() WHERE id = #{userId}")
    int updateNickname(@Param("userId") String userId, @Param("nickname") String nickname);

    @Update("UPDATE users SET gender = #{gender}, updated_at = NOW() WHERE id = #{userId}")
    int updateGender(@Param("userId") String userId, @Param("gender") String gender);

    @Update("UPDATE users SET avatar = #{avatar}, updated_at = NOW() WHERE id = #{userId}")
    int updateAvatar(@Param("userId") String userId, @Param("avatar") String avatar);

    @Update("UPDATE users SET nickname = #{nickname}, gender = #{gender}, updated_at = NOW() WHERE id = #{userId}")
    int updateProfile(@Param("userId") String userId,
                      @Param("nickname") String nickname,
                      @Param("gender") String gender);

}