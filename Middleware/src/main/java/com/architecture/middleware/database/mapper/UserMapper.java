package com.architecture.middleware.database.mapper;

import com.architecture.middleware.database.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM user")
    List<User> findAll();

    @Insert("INSERT INTO user(username, email, age) VALUES(#{username}, #{email}, #{age})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE user SET username=#{username}, email=#{email}, age=#{age} WHERE id=#{id}")
    int update(User user);

    @Delete("DELETE FROM user WHERE id = #{id}")
    int delete(Long id);

    @Select("SELECT * FROM user WHERE username LIKE CONCAT('%', #{keyword}, '%')")
    List<User> searchByUsername(String keyword);
}
