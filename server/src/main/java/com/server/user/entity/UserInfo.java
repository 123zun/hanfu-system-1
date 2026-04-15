package com.server.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("user")
public class UserInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @TableField("username")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    @TableField("password")
    private String password;

    @Email(message = "邮箱格式不正确")
    @TableField("email")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @TableField("phone")
    private String phone;

    @TableField("gender")
    private String gender = "male";

    @TableField("region")
    private String region;

    @TableField("bio")
    private String bio;

    @TableField("age")
    private String age;

    @TableField("avatar")
    private String avatar = "http://localhost:8080/uploads/avatars/default.png";

    @TableField("status")
    private Integer status = 1;  // 1-正常，0-已删除

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}