package com.server.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.activity.entity.ActivitySignup;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ActivitySignupMapper extends BaseMapper<ActivitySignup> {

    // 查询活动的已报名人数
    @Select("SELECT COUNT(*) FROM activity_join WHERE activity_id = #{activityId} AND status = 0")
    int countByActivityId(@Param("activityId") Long activityId);

    // 查询用户的报名记录
    @Select("SELECT COUNT(*) FROM activity_join WHERE activity_id = #{activityId} AND user_id = #{userId} AND status = 0")
    int countByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);

    // 查询活动的报名用户列表（只查主表）
    @Select("SELECT * FROM activity_join WHERE activity_id = #{activityId} AND status = 0 ORDER BY signup_time DESC")
    List<ActivitySignup> selectByActivityId(@Param("activityId") Long activityId);

    // 查询用户是否已报名
    @Select("SELECT * FROM activity_join WHERE activity_id = #{activityId} AND user_id = #{userId} AND status = 0 LIMIT 1")
    ActivitySignup selectByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);

    // 新增报名记录
    @Insert("INSERT INTO activity_join (activity_id, user_id, username, user_avatar, signup_time, status) " +
            "SELECT #{activityId}, #{userId}, u.username, u.avatar, NOW(), 0 " +
            "FROM user u WHERE u.id = #{userId}")
    int insertSignup(@Param("activityId") Long activityId, @Param("userId") Long userId);

    // 删除报名记录
    @Delete("DELETE FROM activity_join WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    // 查询所有可用用户（未被删除的）
    @Select("SELECT u.id, u.username, u.avatar FROM user u WHERE u.status = 1 ORDER BY u.id")
    @Results({
        @Result(column = "username", property = "userName"),
        @Result(column = "avatar", property = "userAvatar")
    })
    List<ActivitySignup> selectAllAvailableUsers();
}
