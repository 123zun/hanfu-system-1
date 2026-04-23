package com.server.recommend.mapper;

import com.server.recommend.entity.UserEmbedding;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface UserEmbeddingMapper {

    @Select("SELECT * FROM user_embedding WHERE user_id = #{userId}")
    UserEmbedding findByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO user_embedding (user_id, embedding_vector, updated_at) VALUES (#{userId}, #{embeddingVector}, NOW()) " +
            "ON DUPLICATE KEY UPDATE embedding_vector = #{embeddingVector}, updated_at = NOW()")
    void saveOrUpdate(UserEmbedding userEmbedding);

    @Delete("DELETE FROM user_embedding")
    void deleteAll();

    @Select("SELECT * FROM user_embedding")
    List<UserEmbedding> findAll();
}
