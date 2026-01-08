CREATE TABLE `social_idle_item`
(
    `id`                bigint         NOT NULL COMMENT '主键ID',
    `thread_id`         bigint         NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint         NOT NULL COMMENT '发布者用户ID（冗余）',
    `school_code`       varchar(50)             DEFAULT NULL COMMENT '学校编码（冗余，可用于校内筛选）',

    `title`             varchar(100)   NOT NULL COMMENT '闲置标题（列表展示/搜索）',
    `category_id`       bigint                  DEFAULT NULL COMMENT '商品类目ID（可选）',
    `brand`             varchar(50)             DEFAULT NULL COMMENT '品牌（可选）',

    `original_price`    decimal(10, 2)          DEFAULT NULL COMMENT '原价',
    `sale_price`        decimal(10, 2) NOT NULL COMMENT '售价',
    `currency`          varchar(10)    NOT NULL DEFAULT 'CNY' COMMENT '币种',

    `condition_level`   tinyint(1)     NOT NULL DEFAULT '0' COMMENT '成色：0未知 1全新 2几乎全新 3轻微使用 4明显使用',
    `trade_mode`        tinyint(1)     NOT NULL DEFAULT '1' COMMENT '交易方式：1当面 2邮寄 3都可',
    `delivery_fee_type` tinyint(1)     NOT NULL DEFAULT '0' COMMENT '运费：0不适用 1包邮 2到付 3自定义',
    `delivery_fee`      decimal(10, 2)          DEFAULT NULL COMMENT '运费金额（delivery_fee_type=3）',

    `stock`             int            NOT NULL DEFAULT '1' COMMENT '数量（默认1）',
    `sale_status`       tinyint(1)     NOT NULL DEFAULT '1' COMMENT '状态：0下架 1在售 2已预订 3已售出',
    `sold_time`         datetime                DEFAULT NULL COMMENT '售出时间',

    -- 用于“附近/最近”检索（GCJ-02）
    `geo_enabled`       tinyint(1)     NOT NULL DEFAULT '0' COMMENT '是否有有效坐标：0否 1是',
    `select_longitude`  decimal(10, 7)          DEFAULT NULL COMMENT '选择位置经度(GCJ-02)',
    `select_latitude`   decimal(10, 7)          DEFAULT NULL COMMENT '选择位置纬度(GCJ-02)',
    `select_location`   point          NOT NULL COMMENT '选择位置POINT(经度,纬度)，无坐标时写POINT(0,0)',

    `create_time`       datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime                DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_thread_id` (`thread_id`) USING BTREE,
    SPATIAL KEY `sp_idx_select_location` (`select_location`)
) ENGINE = InnoDB
    COMMENT ='闲置二手扩展表（与帖子1:1，复用帖子所有互动表）';
