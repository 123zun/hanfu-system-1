package com.server.activity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.activity.entity.Activity;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    // 根据条件查询活动ID列表（用于手动分页）
    @Select("<script>" +
            "SELECT a.* FROM activity a " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND a.title LIKE CONCAT('%', #{keyword}, '%')" +
            "</if>" +
            " ORDER BY a.create_time DESC" +
            "</script>")
    java.util.List<Activity> selectActivityIds(@Param("keyword") String keyword);
}
