package com.server.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.resource.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    // 查询不重复的分类
    @Select("SELECT DISTINCT category FROM resource WHERE status = 1 AND category IS NOT NULL AND category <> '' ORDER BY category")
    List<String> selectCategories();

    // 增加下载次数
    @Update("UPDATE resource SET download_count = download_count + 1 WHERE id = #{id}")
    int increaseDownloadCount(@Param("id") Long id);
}
