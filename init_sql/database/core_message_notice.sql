CREATE TABLE `core_message_notice`
(
    `id`              bigint     NOT NULL COMMENT '主键ID',

    -- 收件箱维度
    `user_id`         bigint     NOT NULL COMMENT '接收用户ID',

    -- 来源（谁触发的：点赞/评论者；系统通知则为0/NULL）
    `sender_user_id`  bigint              DEFAULT NULL COMMENT '触发用户ID（系统通知可为空）',

    -- 领域/类型（用于分频道：互动、系统、活动、运营等）
    `biz_domain`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '业务域：0社交互动 1系统 2活动 3运营/公告 4交易/订单（预留）',
    `notice_type`     tinyint(2) NOT NULL COMMENT '通知类型（域内枚举，自定义扩展）',

    -- 模板化（推荐）
    `template_code`   varchar(100)        DEFAULT NULL COMMENT '通知模板编码（可空：不用模板就直接存文案）',
    `template_params` json                DEFAULT NULL COMMENT '模板参数JSON（可空）',

    -- 直接展示（不走模板时使用）
    `title`           varchar(100)        DEFAULT NULL COMMENT '消息标题',
    `content`         varchar(255)        DEFAULT NULL COMMENT '消息内容（简短，列表展示）',

    -- 媒体快照（可选：消息列表的缩略图/封面）
    `media`           json                DEFAULT NULL COMMENT '媒体JSON（缩略图/封面等）',

    -- 业务对象关联（通用：一条消息最多关联一个主对象）
    `object_type`     tinyint(2)          DEFAULT NULL COMMENT '关联对象类型：1帖子 2评论 3投票 4抽奖 5链接 6公告 7订单（预留）',
    `object_id`       bigint              DEFAULT NULL COMMENT '关联对象ID',
    `object_sub_id`   bigint              DEFAULT NULL COMMENT '子对象ID（如评论ID/奖品ID等，可空）',

    -- 跳转（兼容你 platform_notice / pop_ad 的 click_type 语义）
    `redirect_type`   tinyint(1) NOT NULL DEFAULT '0' COMMENT '跳转类型：0无 1外链 2图片/弹窗 3内部页面 4跳转小程序',
    `redirect_url`    varchar(255)        DEFAULT NULL COMMENT '跳转地址/内部path/外链',
    `app_id`          varchar(255)        DEFAULT NULL COMMENT '小程序appid（redirect_type=4）',
    `redirect_params` json                DEFAULT NULL COMMENT '跳转参数JSON（可空）',

    -- IM/第三方扩展（你之前的 im_object）
    `im_object`       varchar(255)        DEFAULT NULL COMMENT 'IM扩展对象（可空）',

    -- 状态
    `is_read`         tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读：0否 1是',
    `read_time`       datetime            DEFAULT NULL COMMENT '已读时间',
    `is_del`          tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标识：0否 1是',
    `del_time`        datetime            DEFAULT NULL COMMENT '删除时间',

    -- 幂等去重（强烈建议）
    `dedupe_key`      varchar(64)         DEFAULT NULL COMMENT '幂等去重键（可空；建议对互动类/活动类生成）',

    -- 排序/置顶（消息中心常见需求）
    `priority`        int        NOT NULL DEFAULT '0' COMMENT '优先级/权重（越大越靠前）',
    `push_status`     tinyint(1) NOT NULL DEFAULT '0' COMMENT '推送状态：0未推送 1已推送（可选）',

    `create_time`     datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_dedupe_key` (`dedupe_key`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='全站通知收件箱（社交/系统/活动/运营统一）';
