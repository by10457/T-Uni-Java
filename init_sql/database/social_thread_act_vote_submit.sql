CREATE TABLE `social_thread_vote_submit`
(
    `id`           bigint NOT NULL COMMENT '主键ID',

    `vote_id`      bigint NOT NULL COMMENT '投票ID（social_thread_vote.id）',
    `thread_id`    bigint NOT NULL COMMENT '帖子ID（social_thread.id）',
    `vote_user_id` bigint NOT NULL COMMENT '投票用户ID',

    `choice_count` int    NOT NULL DEFAULT '0' COMMENT '本次提交选择的选项数',
    `client_ip`    varchar(45)     DEFAULT NULL COMMENT '客户端IP（可选，风控用）',
    `user_agent`   varchar(255)    DEFAULT NULL COMMENT 'UA（可选，风控用）',

    `create_time`  datetime        DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `update_time`  datetime        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_vote_user` (`vote_id`, `vote_user_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子投票提交表（保证一人只能提交一次）';
