package com.server.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.message.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}