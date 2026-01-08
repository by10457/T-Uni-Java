CREATE TABLE `social_thread_topic_rel`
(
    `topic_id`    bigint NOT NULL COMMENT '话题ID（social_topic.id）',
    `thread_id`   bigint NOT NULL COMMENT '帖子ID（social_thread.id）',

    `school_code` varchar(50) DEFAULT NULL COMMENT '学校编码（冗余，便于按学校筛选话题帖子）',

    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`topic_id`, `thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='话题-帖子关联表';
