CREATE TABLE `social_thread_comment`
(
    `id`                bigint                       NOT NULL COMMENT '主键ID',

    `thread_id`         bigint                       NOT NULL COMMENT '帖子ID（social_thread.id）',
    `publisher_user_id` bigint                                DEFAULT NULL COMMENT '帖子发布者ID（冗余，便于通知/运营）',

    `user_id`           bigint                       NOT NULL COMMENT '评论/回复者用户ID',
    `content`           text COMMENT '评论内容',
    `image_url`         json                                  DEFAULT NULL COMMENT '评论图片/媒体',

    -- 评论层级结构（评论/回复同表）
    `parent_id`         bigint                       NOT NULL DEFAULT '0' COMMENT '父评论ID（顶级=0）',
    `reply_comment_id`  bigint                                DEFAULT NULL COMMENT '被回复的评论ID（可空，等同 parent_id 或用于二级回复指向）',
    `reply_user_id`     bigint                                DEFAULT NULL COMMENT '被回复的用户ID',

    `aite_ids`          json                                  DEFAULT NULL COMMENT '@的用户id集合',

    -- 互动计数（缓存）
    `like_count`        int                          NOT NULL DEFAULT '0' COMMENT '点赞量',
    `report_count`      int                          NOT NULL DEFAULT '0' COMMENT '举报量',

    -- 审核/展示/删除/隐藏
    `check_status`      tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '审核状态（0待审核、1审核中、2成功、3失败、4人工审核）',
    `check_reason`      varchar(255)                          DEFAULT NULL COMMENT '未过审原因',

    `is_del`            tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '是否删除（0否 1是）',
    `del_time`          datetime                              DEFAULT NULL COMMENT '删除时间',

    `is_show`           tinyint(1)                   NOT NULL DEFAULT '1' COMMENT '是否显示（0否 1是）',
    `is_hide`           tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '是否被管理员隐藏（0否 1是）',

    -- 匿名
    `is_anonymous`      tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '是否匿名（0否 1是）',
    `anonymous_id`      bigint                                DEFAULT NULL COMMENT '匿名信息ID',

    -- IP属地（评论发布时采集，非强依赖）
    `comment_ip`        varchar(45)                           DEFAULT NULL COMMENT '评论IP',
    `ip_province`       varchar(100)                          DEFAULT NULL COMMENT 'IP省份',

    `create_time`       datetime                              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime                              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子评论表（评论/回复同表）';
