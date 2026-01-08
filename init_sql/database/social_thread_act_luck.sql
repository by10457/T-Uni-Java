CREATE TABLE `social_thread_luck`
(
    `id`               bigint     NOT NULL COMMENT '主键ID',
    `thread_id`        bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`          bigint     NOT NULL COMMENT '发起人用户ID',

    `luck_type`        tinyint(1) NOT NULL DEFAULT '1' COMMENT '抽奖条件：1点赞 2评论 3赞+评论 4参与即算',
    `winner_count`     int        NOT NULL DEFAULT '1' COMMENT '中奖人数',
    `prize_desc`       varchar(255)        DEFAULT NULL COMMENT '奖品描述（简述）',
    `notice_remark`    text COMMENT '通知信息',
    `receive_remark`   text COMMENT '领奖信息/规则',

    `close_time`       datetime            DEFAULT NULL COMMENT '截止时间（到期停止参与）',
    `open_time`        datetime            DEFAULT NULL COMMENT '开奖时间（可为空，空则按close_time或手动）',
    `open_mode`        tinyint(1) NOT NULL DEFAULT '1' COMMENT '开奖方式：1到时自动 2手动开奖 3提前开奖（open_time）',

    `luck_status`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '抽奖状态：0未开奖 1已开奖 2已取消',
    `check_status`     tinyint(1) NOT NULL DEFAULT '0' COMMENT '审核状态（0待审核、1审核中、2成功、3失败、4人工审核）',

    `open_operator_id` bigint              DEFAULT NULL COMMENT '开奖操作人ID（管理员/代理等，可空）',
    `open_real_time`   datetime            DEFAULT NULL COMMENT '实际开奖时间',

    `is_del`           tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标识（0未删除 1已删除）',
    `del_time`         datetime            DEFAULT NULL COMMENT '删除时间',

    `create_time`      datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_thread_id` (`thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子抽奖活动表';
