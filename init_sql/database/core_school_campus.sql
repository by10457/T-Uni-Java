CREATE TABLE `core_school_campus`
(
    `id`           bigint         NOT NULL COMMENT '主键ID',

    `school_code`  varchar(50)  DEFAULT NULL COMMENT '学校编码（关联 core_school.school_code）',
    `campus_code`  varchar(50)  DEFAULT NULL COMMENT '校区编码',
    `campus_name`  varchar(100) DEFAULT NULL COMMENT '校区名字',
    `campus_cover` varchar(150) DEFAULT NULL COMMENT '校区封面图',

    `province`     varchar(100) DEFAULT NULL COMMENT '省份',
    `city`         varchar(100) DEFAULT NULL COMMENT '城市',

    `longitude`    decimal(10, 7) NOT NULL COMMENT '经度(GCJ-02)',
    `latitude`     decimal(10, 7) NOT NULL COMMENT '纬度(GCJ-02)',
    `location`     point          NOT NULL COMMENT '位置信息POINT(经度,纬度)（GCJ-02）',

    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_campus_code` (`campus_code`) USING BTREE,
    SPATIAL KEY `sp_idx_location` (`location`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='核心校区信息表（用于附近搜索，坐标系GCJ-02）';
