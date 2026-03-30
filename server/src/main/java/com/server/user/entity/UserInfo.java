package com.server.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.server.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class UserInfo extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "username")
    private String username;

    @JsonIgnore
    @TableField(value = "password")
    private String password;

    @TableField(value = "email")
    private String email;

    @TableField(value = "phone")
    private String phone;

    @TableField(value = "gender")
    private String gender;

    @TableField(value = "avatar")
    private String avatar;

    @TableField(value = "status")
    private Integer status = 1;

    @TableField(value = "role")
    private String role = "USER";
}
