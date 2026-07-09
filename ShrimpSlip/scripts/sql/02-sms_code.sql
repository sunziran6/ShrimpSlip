USE shrimpslip;

CREATE TABLE IF NOT EXISTS `sms_code` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `phone`      VARCHAR(20) NOT NULL COMMENT '手机号',
    `code`       VARCHAR(6)  NOT NULL COMMENT '6位验证码',
    `type`       TINYINT     NOT NULL COMMENT '1=注册 2=登录',
    `used`       TINYINT     NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用',
    `expires_at` DATETIME    NOT NULL COMMENT '过期时间',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_phone_type` (`phone`, `type`),
    INDEX `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码表';
