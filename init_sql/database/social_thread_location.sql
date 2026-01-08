CREATE TABLE `social_thread_location`
(
    `id`                bigint     NOT NULL COMMENT '主键ID',

    `thread_id`         bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint     NOT NULL COMMENT '发布者用户ID',

    -- 发布时环境信息（风控/校验用）
    `publish_ip`        varchar(45)         DEFAULT NULL COMMENT '发布IP',
    `ip_country`        varchar(50)         DEFAULT NULL COMMENT 'IP解析国家',
    `ip_province`       varchar(100)        DEFAULT NULL COMMENT 'IP解析省份',
    `ip_city`           varchar(100)        DEFAULT NULL COMMENT 'IP解析城市',
    `ip_region`         varchar(100)        DEFAULT NULL COMMENT 'IP解析区/县',
    `ip_adcode`         varchar(20)         DEFAULT NULL COMMENT 'IP解析行政区划码（可选）',

    -- 发布时定位（用户设备定位，可能不展示）
    `publish_longitude` decimal(10, 7)      DEFAULT NULL COMMENT '发布定位经度(GCJ-02)',
    `publish_latitude`  decimal(10, 7)      DEFAULT NULL COMMENT '发布定位纬度(GCJ-02)',
    `publish_location`  point               DEFAULT NULL COMMENT '发布定位POINT(经度,纬度)',

    `publish_address`   varchar(255)        DEFAULT NULL COMMENT '发布地址全称（逆地理结果）',
    `publish_province`  varchar(100)        DEFAULT NULL COMMENT '发布省份',
    `publish_city`      varchar(100)        DEFAULT NULL COMMENT '发布城市',
    `publish_district`  varchar(100)        DEFAULT NULL COMMENT '发布区/县',

    -- 用户选择展示的位置（用于对外展示/附近搜索）
    `select_name`       varchar(255)        DEFAULT NULL COMMENT '用户选择的地点名称/POI',
    `select_longitude`  decimal(10, 7)      DEFAULT NULL COMMENT '选择位置经度(GCJ-02)',
    `select_latitude`   decimal(10, 7)      DEFAULT NULL COMMENT '选择位置纬度(GCJ-02)',
    `select_location`   point               DEFAULT NULL COMMENT '选择位置POINT(经度,纬度)',
    `select_address`    varchar(255)        DEFAULT NULL COMMENT '选择位置全称地址',

    -- 展示与隐私
    `is_show`           tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否展示定位：0否 1是',
    `precision_level`   tinyint(1) NOT NULL DEFAULT '2' COMMENT '展示精度：1省 2市 3区 4街道 5POI',
    `geo_provider`      tinyint(1)          DEFAULT NULL COMMENT '地理服务来源：1高德 2腾讯 3百度 4其他',

    `create_time`       datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_thread_id` (`thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子位置信息表（发布环境+用户选择位置）';
