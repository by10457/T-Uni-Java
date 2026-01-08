CREATE TABLE `social_thread_report`
(
    `id`                bigint     NOT NULL COMMENT '主键ID',
    `thread_id`         bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `report_user_id`    bigint     NOT NULL COMMENT '举报用户ID',
    `publisher_user_id` bigint              DEFAULT NULL COMMENT '帖子发布者ID（冗余）',

    `report_type`       tinyint(1)          DEFAULT NULL COMMENT '举报类型：1涉黄 2违法 3辱骂 4广告 5骚扰 6其他（可扩展）',
    `report_reason`     varchar(255)        DEFAULT NULL COMMENT '举报原因（补充说明）',
    `evidence_urls`     json                DEFAULT NULL COMMENT '证据图片/链接（可多张）',

    `status`            tinyint(1) NOT NULL DEFAULT '0' COMMENT '处理状态：0待处理 1已处理 2驳回 3忽略',
    `handler_id`        bigint              DEFAULT NULL COMMENT '处理人ID（管理员/代理）',
    `handle_time`       datetime            DEFAULT NULL COMMENT '处理时间',
    `handle_remark`     varchar(255)        DEFAULT NULL COMMENT '处理备注',

    `create_time`       datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子举报表（工单型）';
