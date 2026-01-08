CREATE TABLE `social_thread`
(
    `id`             bigint                       NOT NULL COMMENT '主键ID',

    `user_id`        bigint                       NOT NULL COMMENT '发布者ID',

    `school_code`    varchar(50)                           DEFAULT NULL COMMENT '学校code',
    `campus_code`    varchar(50)                           DEFAULT NULL COMMENT '校区code',

    `biz_type`       tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '业务类型：0普通帖子 1闲置二手',

    `content`        text COMMENT '帖子内容',
    `media_url`      json                                  DEFAULT NULL COMMENT '图片/视频链接',

    -- 互动计数（缓存）
    `comment_count`  int                          NOT NULL DEFAULT '0' COMMENT '评论数量',
    `comment_time`   datetime                              DEFAULT NULL COMMENT '最新评论时间',
    `like_count`     int                          NOT NULL DEFAULT '0' COMMENT '点赞量',
    `view_count`     int                          NOT NULL DEFAULT '0' COMMENT '浏览量',
    `share_count`    int                          NOT NULL DEFAULT '0' COMMENT '分享量',
    `keep_count`     int                          NOT NULL DEFAULT '0' COMMENT '插眼量',
    `collect_count`  int                          NOT NULL DEFAULT '0' COMMENT '收藏量',
    `report_count`   int                          NOT NULL DEFAULT '0' COMMENT '举报量',

    -- 活动（一个帖子只能挂一个活动）
    `activity_type`  tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '活动类型：0无活动 1投票 2抽奖 3跳转链接',
    `activity_id`    bigint                                DEFAULT NULL COMMENT '活动记录ID（对应活动表主键）',

    -- 可见性
    `is_public`      tinyint(1)                   NOT NULL DEFAULT '1' COMMENT '是否公开：0私密 1公开',
    `is_school`      tinyint(1)                   NOT NULL DEFAULT '1' COMMENT '是否仅发布到校内：0否 1是',
    `is_auth`        tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '是否仅认证可见：0否 1是',

    -- 匿名
    `is_anonymous`   tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '是否匿名：0否 1是',
    `anonymous_id`   bigint                                DEFAULT NULL COMMENT '匿名信息关联ID',

    -- 评论开关
    `is_comment`     tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '是否关闭评论：0否 1是',

    -- 审核
    `check_status`   tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '审核状态（0待审核、1审核中、2成功、3失败、4人工审核）',
    `reject_reason`  text COMMENT '未过审原因',

    -- 删除/隐藏/推送
    `is_del`         tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '删除标识（0未删除 1已删除）',
    `del_time`       datetime                              DEFAULT NULL COMMENT '删除时间',
    `is_push`        tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '推送标识（0未推送 1已推送）',
    `is_hide`        tinyint(1) unsigned zerofill NOT NULL DEFAULT '0' COMMENT '是否被管理员隐藏（0否 1是）',

    -- 关联信息
    `aite_ids`       json                                  DEFAULT NULL COMMENT '@关联字段',
    `topic_ids`      json                                  DEFAULT NULL COMMENT '话题ids（冗余展示用，筛选走关联表更好）',

    -- 置顶/排序
    `is_sticky`      tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '是否置顶：0否 1是',
    `is_sticky_type` tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '置顶类型（0否、1用户帮顶、2用户置顶、3代理置顶、4系统置顶）',

    -- 热度
    `is_fake`        tinyint(1)                   NOT NULL DEFAULT '0' COMMENT '是否伪造数据（0否 1是）',
    `hot`            int                          NOT NULL DEFAULT '0' COMMENT '帖子热度',

    -- 时间字段
    `publish_time`   datetime                              DEFAULT NULL COMMENT '发布时间',
    `polish_count`   bigint                       NOT NULL DEFAULT '0' COMMENT '擦亮次数',
    `polish_time`    datetime                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '擦亮时间（用于排序）',

    `create_time`    datetime                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='社区帖子表';
