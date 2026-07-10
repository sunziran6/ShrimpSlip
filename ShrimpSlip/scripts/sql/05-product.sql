CREATE TABLE product (
    id BIGINT PRIMARY KEY COMMENT '商品ID（Snowflake）',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    image VARCHAR(500) COMMENT '商品图片URL',
    category VARCHAR(100) COMMENT '商品分类',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=下架 1=上架',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';
