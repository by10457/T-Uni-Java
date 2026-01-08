CREATE TABLE `social_thread_share`
(
    `id`                bigint NOT NULL COMMENT '主键ID',
    `thread_id`         bigint NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint NOT NULL COMMENT '转发用户ID',
    `publisher_user_id` bigint      DEFAULT NULL COMMENT '帖子发布者ID（冗余）',

    `share_channel`     tinyint(1)  DEFAULT NULL COMMENT '渠道：1微信好友 2朋友圈 3复制链接 4保存图片 5其他',
    `share_target`      varchar(64) DEFAULT NULL COMMENT '目标（可选：群/会话/渠道标识）',

    `create_time`       datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '转发时间',
    `update_time`       datetime    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',


    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子转发日志表（每次转发一条）';
