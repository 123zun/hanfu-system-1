package com.server.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.user.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<UserInfo> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    UserInfo selectByUsername(String username);

    @Select("SELECT * FROM user WHERE email = #{email}")
    UserInfo selectByEmail(String email);

    @Select("SELECT * FROM user WHERE phone = #{phone}")
    UserInfo selectByPhone(String phone);
}
