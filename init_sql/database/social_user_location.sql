CREATE TABLE `core_user_location`
(
    `id`           bigint NOT NULL COMMENT '主键ID',
    `user_id`      bigint NOT NULL COMMENT '用户ID（core_user.id）',

    -- 原始定位输入
    `ip`           varchar(45)    DEFAULT NULL COMMENT '定位IP（原始，支持IPv4/IPv6）',
    `longitude`    decimal(10, 7) DEFAULT NULL COMMENT '经度（原始）',
    `latitude`     decimal(10, 7) DEFAULT NULL COMMENT '纬度（原始）',
    `accuracy_m`   int            DEFAULT NULL COMMENT '定位精度（米，可选）',

    -- 经纬度逆地理编码结果（由经纬度推断）
    `province`     varchar(100)   DEFAULT NULL COMMENT '省份（逆地理编码）',
    `city`         varchar(100)   DEFAULT NULL COMMENT '城市（逆地理编码）',
    `district`     varchar(100)   DEFAULT NULL COMMENT '区（逆地理编码）',
    `street`       varchar(255)   DEFAULT NULL COMMENT '街道（逆地理编码）',
    `address`      varchar(255)   DEFAULT NULL COMMENT '完整地址（逆地理编码）',

    -- IP 解析属地结果（由IP推断）
    `ip_region`    varchar(255)   DEFAULT NULL COMMENT 'IP属地（文本，如：浙江省·杭州市）',
    `ip_province`  varchar(100)   DEFAULT NULL COMMENT 'IP属地-省（可选，便于统计）',
    `ip_city`      varchar(100)   DEFAULT NULL COMMENT 'IP属地-市（可选，便于统计）',

    -- 解析/来源元数据（可追溯）
    `loc_source`   tinyint        DEFAULT '0' COMMENT '定位来源：0未知/1GPS/2基站/3WiFi/4IP/5手动',
    `geo_provider` varchar(50)    DEFAULT 'Tencent' COMMENT '逆地理/属地解析服务商（如Amap/Tencent/Baidu/自建）',
    `geo_time`     datetime       DEFAULT NULL COMMENT '最近一次完成解析的时间',

    `create_time`  datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime       DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
    COMMENT ='核心用户定位表（原始定位 + 逆地理编码 + IP属地）';
