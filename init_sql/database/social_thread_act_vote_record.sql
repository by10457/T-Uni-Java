CREATE TABLE `social_thread_vote_record`
(
    `id`           bigint NOT NULL COMMENT '主键ID',

    `vote_id`      bigint NOT NULL COMMENT '投票ID（social_thread_vote.id）',
    `thread_id`    bigint NOT NULL COMMENT '帖子ID（social_thread.id）',
    `option_id`    bigint NOT NULL COMMENT '选项ID（social_thread_vote_option.id）',

    `vote_user_id` bigint NOT NULL COMMENT '投票用户ID',

    `create_time`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    `update_time`  datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_vote_user_option` (`vote_id`, `vote_user_id`, `option_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子用户投票记录表';
