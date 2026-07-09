package com.shrimpslip.app.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String phone;
    private String password;
    private String nickname;
    private String avatar;
    private Integer status;
    private String role;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
