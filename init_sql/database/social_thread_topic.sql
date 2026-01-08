CREATE TABLE `social_thread_topic`
(
    `id`              bigint       NOT NULL COMMENT '主键ID',

    `topic_name`      varchar(255) NOT NULL COMMENT '话题名',
    `topic_desc`      varchar(255)          DEFAULT NULL COMMENT '话题描述/简介',
    `background_url`  varchar(255)          DEFAULT NULL COMMENT '话题背景图',

    -- 可见性范围
    `scope_type`      tinyint(1)   NOT NULL DEFAULT '0' COMMENT '可见范围：0全平台 1指定学校可见 2指定学校代理可见',
    `school_code`     varchar(50)           DEFAULT NULL COMMENT '学校编码（scope_type=1/2 时使用）',

    -- 创建与属性
    `creator_user_id` bigint                DEFAULT NULL COMMENT '创建话题用户ID（可为0即官方话题）',
    `is_official`     tinyint(1)   NOT NULL DEFAULT '0' COMMENT '是否官方话题：0否 1是',
    `is_hot`          tinyint(1)   NOT NULL DEFAULT '0' COMMENT '是否热门：0否 1是',
    `hot`             int          NOT NULL DEFAULT '0' COMMENT '热度（用于排序）',

    -- 统计计数（缓存）
    `view_count`      int          NOT NULL DEFAULT '0' COMMENT '阅读量（真实）',
    `comment_count`   int          NOT NULL DEFAULT '0' COMMENT '讨论量（真实）',
    `fake_view`       int          NOT NULL DEFAULT '0' COMMENT '虚拟浏览',
    `fake_comment`    int          NOT NULL DEFAULT '0' COMMENT '虚拟讨论',

    -- 状态控制
    `status`          tinyint(1)   NOT NULL DEFAULT '1' COMMENT '状态：0禁用 1启用',
    `is_del`          tinyint(1)   NOT NULL DEFAULT '0' COMMENT '删除标识：0否 1是',
    `del_time`        datetime              DEFAULT NULL COMMENT '删除时间',

    `create_time`     datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_topic_name_scope` (`topic_name`, `scope_type`, `school_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='社区话题表';
