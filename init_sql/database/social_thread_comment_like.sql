CREATE TABLE `social_thread_comment_like`
(
    `id`              bigint     NOT NULL COMMENT '主键ID',

    `thread_id`       bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `comment_id`      bigint     NOT NULL COMMENT '评论ID（social_thread_comment.id）',

    `user_id`         bigint     NOT NULL COMMENT '点赞用户ID',
    `comment_user_id` bigint              DEFAULT NULL COMMENT '评论发布者用户ID（冗余，便于通知）',

    `status`          tinyint(1) NOT NULL DEFAULT '1' COMMENT '点赞状态：1点赞 0取消',
    `cancel_time`     datetime            DEFAULT NULL COMMENT '取消时间（status=0时）',

    `create_time`     datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '首次点赞时间',
    `update_time`     datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='评论点赞表（状态表，一人一评一条）';
