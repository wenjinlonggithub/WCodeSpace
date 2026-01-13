package com.okx.finance.user.mapper;

import com.okx.finance.common.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user (id, username, password, email, phone, kyc_level, status, create_time) " +
            "VALUES (#{id}, #{username}, #{password}, #{email}, #{phone}, #{kycLevel}, #{status}, NOW())")
    void insert(User user);

    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User findByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id} AND deleted = 0")
    User findById(Long id);

    @Update("UPDATE user SET real_name = #{realName}, id_card = #{idCard}, kyc_level = #{kycLevel}, " +
            "update_time = NOW() WHERE id = #{id}")
    void update(User user);

    @Update("UPDATE user SET api_key = #{apiKey}, api_secret = #{apiSecret}, update_time = NOW() WHERE id = #{id}")
    void updateApiKey(User user);
}
