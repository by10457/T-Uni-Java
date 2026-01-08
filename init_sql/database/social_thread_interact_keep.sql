CREATE TABLE `social_thread_keep`
(
    `id`                bigint     NOT NULL COMMENT '主键ID',
    `thread_id`         bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint     NOT NULL COMMENT '插眼用户ID',
    `publisher_user_id` bigint              DEFAULT NULL COMMENT '帖子发布者ID（冗余）',

    `status`            tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1插眼 0取消',
    `cancel_time`       datetime            DEFAULT NULL COMMENT '取消时间（status=0时）',

    `create_time`       datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '首次插眼时间',
    `update_time`       datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_thread` (`user_id`, `thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子插眼表（状态表，一人一贴一条）';
