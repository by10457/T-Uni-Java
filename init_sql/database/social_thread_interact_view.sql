CREATE TABLE `social_thread_view`
(
    `id`                bigint NOT NULL COMMENT '主键ID',
    `thread_id`         bigint NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint NOT NULL COMMENT '浏览用户ID',
    `publisher_user_id` bigint          DEFAULT NULL COMMENT '帖子发布者ID（冗余）',

    `view_count`        int    NOT NULL DEFAULT '1' COMMENT '累计浏览次数（同一用户）',
    `first_view_time`   datetime        DEFAULT CURRENT_TIMESTAMP COMMENT '首次浏览时间',
    `last_view_time`    datetime        DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次浏览时间',

    `create_time`       datetime        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_thread` (`user_id`, `thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子浏览表（聚合表，一人一贴一行）';
