CREATE TABLE `social_thread_luck_prize`
(
    `id`          bigint       NOT NULL COMMENT '主键ID',

    `luck_id`     bigint       NOT NULL COMMENT '抽奖ID（social_thread_luck.id）',
    `thread_id`   bigint       NOT NULL COMMENT '帖子ID（social_thread.id，冗余便于查询）',

    `prize_title` varchar(255) NOT NULL COMMENT '奖品名称',
    `prize_desc`  varchar(255)          DEFAULT NULL COMMENT '奖品描述（简述）',
    `prize_image` json                  DEFAULT NULL COMMENT '奖品图片/描述图（可多张）',

    `prize_count` int          NOT NULL DEFAULT '1' COMMENT '该奖品名额数',
    `win_count`   int          NOT NULL DEFAULT '0' COMMENT '已中奖人数（缓存计数）',

    `sort`        int          NOT NULL DEFAULT '0' COMMENT '排序（越大越靠前）',
    `is_del`      tinyint(1)   NOT NULL DEFAULT '0' COMMENT '删除标识（0未删除 1已删除）',

    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子抽奖奖品表（一个抽奖多个奖品档位）';
