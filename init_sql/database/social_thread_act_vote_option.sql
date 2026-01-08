CREATE TABLE `social_thread_vote_option`
(
    `id`               bigint       NOT NULL COMMENT '主键ID',
    `vote_id`          bigint       NOT NULL COMMENT '投票ID（social_thread_vote.id）',
    `thread_id`        bigint       NOT NULL COMMENT '帖子ID（social_thread.id，冗余便于查询）',

    `option_content`   varchar(200) NOT NULL COMMENT '选项内容',
    `option_desc`      varchar(255)          DEFAULT NULL COMMENT '选项描述（可空）',
    `option_media_url` varchar(255)          DEFAULT NULL COMMENT '选项图片/媒体（可空）',

    `vote_count`       int          NOT NULL DEFAULT '0' COMMENT '选择该选项的人数（缓存计数）',
    `sort`             int          NOT NULL DEFAULT '0' COMMENT '排序（越大越靠前）',
    `is_del`           tinyint(1)   NOT NULL DEFAULT '0' COMMENT '删除标识（0未删除 1已删除）',

    `create_time`      datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子投票选项表';
