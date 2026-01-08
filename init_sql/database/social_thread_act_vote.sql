CREATE TABLE `social_thread_vote`
(
    `id`           bigint     NOT NULL COMMENT '主键ID',
    `thread_id`    bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`      bigint     NOT NULL COMMENT '发起人用户ID',

    `vote_title`   varchar(255)        DEFAULT NULL COMMENT '投票标题（可空，默认取帖子摘要）',
    `vote_desc`    varchar(255)        DEFAULT NULL COMMENT '投票描述/副标题',

    `close_method` tinyint(1) NOT NULL DEFAULT '2' COMMENT '截止方式：1手动截止 2指定时间',
    `close_time`   datetime            DEFAULT NULL COMMENT '指定关闭时间（close_method=2）',
    `hand_time`    datetime            DEFAULT NULL COMMENT '手动截止时间（close_method=1）',

    `vote_status`  tinyint(1) NOT NULL DEFAULT '0' COMMENT '投票状态：0进行中 1已结束 2已取消',
    `check_status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '审核状态（0待审核、1审核中、2成功、3失败、4人工审核）',

    `user_number`  int        NOT NULL DEFAULT '0' COMMENT '参与人数（缓存值）',
    `vote_count`   int        NOT NULL DEFAULT '0' COMMENT '总投票数（缓存值）',

    `is_multi`     tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否多选：0单选 1多选',
    `max_choice`   int                 DEFAULT NULL COMMENT '最多可选项数（多选时）',

    `is_del`       tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标识（0未删除 1已删除）',
    `del_time`     datetime            DEFAULT NULL COMMENT '删除时间',

    `create_time`  datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_thread_id` (`thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子投票活动表（一个帖子仅一个投票）';
