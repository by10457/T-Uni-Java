CREATE TABLE `social_thread_luck_winner`
(
    `id`                bigint     NOT NULL COMMENT '主键ID',

    `luck_id`           bigint     NOT NULL COMMENT '抽奖ID（social_thread_luck.id）',
    `thread_id`         bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `prize_id`          bigint     NOT NULL COMMENT '奖品ID（social_thread_luck_prize.id）',

    `publisher_user_id` bigint     NOT NULL COMMENT '发起人用户ID（冗余，便于查询/对账）',
    `winner_user_id`    bigint     NOT NULL COMMENT '中奖用户ID',
    `verify_code`       varchar(32)         DEFAULT NULL COMMENT '领奖核验唯一码（用于确认中奖用户身份）',

    `win_no`            int        NOT NULL DEFAULT '0' COMMENT '中奖序号/排序（用于展示）',

    `win_time`          datetime            DEFAULT NULL COMMENT '中奖时间（开奖时写入）',

    `receive_status`    tinyint(1) NOT NULL DEFAULT '0' COMMENT '领奖状态：0未领取 1已提交领取信息 2已发放 3已过期 4已作废',
    `receive_time`      datetime            DEFAULT NULL COMMENT '领取时间',
    `receive_remark`    varchar(255)        DEFAULT NULL COMMENT '领取备注/说明',

    `create_time`       datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_luck_winner` (`luck_id`, `winner_user_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子抽奖中奖记录表';
