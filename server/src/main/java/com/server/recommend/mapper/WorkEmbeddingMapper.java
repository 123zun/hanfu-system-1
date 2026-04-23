package com.server.recommend.mapper;

import com.server.recommend.entity.WorkEmbedding;
import com.server.work.entity.Work;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WorkEmbeddingMapper {

    @Select("SELECT * FROM work_embedding WHERE work_id = #{workId}")
    WorkEmbedding findByWorkId(@Param("workId") Long workId);

    @Insert("INSERT INTO work_embedding (work_id, embedding_vector, updated_at) VALUES (#{workId}, #{embeddingVector}, NOW()) " +
            "ON DUPLICATE KEY UPDATE embedding_vector = #{embeddingVector}, updated_at = NOW()")
    void saveOrUpdate(WorkEmbedding workEmbedding);

    @Delete("DELETE FROM work_embedding")
    void deleteAll();

    @Select("SELECT * FROM work_embedding")
    List<WorkEmbedding> findAll();

    @Select("SELECT * FROM work_embedding WHERE work_id IN (${ids})")
    List<WorkEmbedding> findByWorkIds(@Param("ids") String ids);

    // 查询超过指定分钟数未更新的embedding
    @Select("SELECT we.* FROM work_embedding we INNER JOIN work w ON we.work_id = w.id " +
           "WHERE w.status = 1 AND we.updated_at < DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE)")
    List<WorkEmbedding> findStaleEmbeddings(@Param("minutes") Integer minutes);

    // 查询没有embedding的作品
    @Select("SELECT w.* FROM work w WHERE w.status = 1 AND w.id NOT IN " +
           "(SELECT IFNULL(work_id, 0) FROM work_embedding)")
    List<Work> findWorksWithoutEmbedding();
}
