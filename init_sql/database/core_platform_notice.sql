CREATE TABLE `core_platform_notice`
(
    `id`            bigint     NOT NULL COMMENT '主键ID',

    `title`         varchar(100)        DEFAULT NULL COMMENT '标题',
    `content`       varchar(255)        DEFAULT NULL COMMENT '通知文案/描述',
    `notice_type`   tinyint(1) NOT NULL DEFAULT '1' COMMENT '通知形态：1文字通告栏 2图片弹窗 3轮播图 4贴图 ... 可自行标记扩展',
    `media_url`     varchar(255)        DEFAULT NULL COMMENT '媒体地址（图片/海报等，notice_type=2时常用）',

    -- 区域控制
    `school_code`   varchar(100)        DEFAULT NULL COMMENT '投放学校（空=全平台）',
    `biz_tag`       varchar(50)         DEFAULT NULL COMMENT '业务标记（如：edu/social/hygiene，可按需）',
    `place`         tinyint(1) NOT NULL COMMENT '展示位置（1帖子 2消息 3个人中心 可扩展）',
    `page_path`     varchar(255)        DEFAULT NULL COMMENT '平台内部页面路径/路由（精确到某页面，可空）',

    -- 操作点击效果
    `click_type`    tinyint    NOT NULL DEFAULT '0' COMMENT '点击效果：0无 1跳转外链 2预览图片 3内部跳转 4跳转小程序',
    `redirect_url`  varchar(255)        DEFAULT NULL COMMENT '跳转地址（外链/内部路由等，按click_type解释）',
    `app_id`        varchar(255)        DEFAULT NULL COMMENT '小程序appId（click_type=4时使用）',

    -- 展示频控（弹窗/通知频率控制）
    `pop_type`      tinyint(1) NOT NULL DEFAULT '1' COMMENT '频控类型：1仅一次 2每天一次 3间隔时间 4每次打开都展示',
    `interval_time` int                 DEFAULT NULL COMMENT '间隔时间（分钟，pop_type=3时生效）',
    `start_time`    datetime            DEFAULT NULL COMMENT '开始展示时间',
    `end_time`      datetime            DEFAULT NULL COMMENT '结束展示时间',

    `is_official`   tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否官方：0否 1是',
    `priority`      int        NOT NULL DEFAULT '0' COMMENT '优先级（越大越优先）',

    `is_show`       tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否展示：0否 1是',
    `is_del`        tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除：0否 1是',

    `create_time`   datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='平台通知/广告配置表（通告栏/滚动/弹窗统一，含频控）';
