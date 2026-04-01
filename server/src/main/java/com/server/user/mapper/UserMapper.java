package com.server.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.user.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<UserInfo> {

    // 使用 MyBatis-Plus 的 BaseMapper，已经有很多默认方法：
    // selectById, selectList, selectCount, insert, updateById, deleteById 等

    // 自定义方法：根据用户名查询
    @Select("SELECT * FROM user WHERE username = #{username}")
    UserInfo selectByUsername(String username);

    // 自定义方法：根据邮箱查询
    @Select("SELECT * FROM user WHERE email = #{email}")
    UserInfo selectByEmail(String email);

    // 自定义方法：根据手机号查询
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    UserInfo selectByPhone(String phone);

    // 自定义方法：修改密码
    @Update("UPDATE user SET password = #{password} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    // 复杂查询：条件查询用户列表（在XML中实现）
    List<UserInfo> selectByCondition(Map<String, Object> params);

    // 复杂查询：统计数量（在XML中实现）
    int countByCondition(Map<String, Object> params);

    // 批量删除
    @Delete("<script>" +
            "DELETE FROM user WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteBatch(@Param("ids") List<Long> ids);
}