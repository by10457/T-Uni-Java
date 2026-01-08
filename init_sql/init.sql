CREATE DATABASE IF NOT EXISTS wxy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE wxy;

DROP TRIGGER IF EXISTS tr_core_school_campus_bi_location;
DROP TRIGGER IF EXISTS tr_core_school_campus_bu_location;
DROP TRIGGER IF EXISTS tr_social_idle_item_bi_geo;
DROP TRIGGER IF EXISTS tr_social_idle_item_bu_geo;
DROP TRIGGER IF EXISTS tr_social_thread_location_bi_point;
DROP TRIGGER IF EXISTS tr_social_thread_location_bu_point;
DROP TABLE IF EXISTS core_message_notice;
DROP TABLE IF EXISTS core_platform_notice;
DROP TABLE IF EXISTS core_school;
DROP TABLE IF EXISTS core_school_campus;
DROP TABLE IF EXISTS core_user;
DROP TABLE IF EXISTS core_user_default_avatar;
DROP TABLE IF EXISTS core_user_default_nick_name;
DROP TABLE IF EXISTS edu_school;
DROP TABLE IF EXISTS edu_user;
DROP TABLE IF EXISTS hygiene_user;
DROP TABLE IF EXISTS social_thread;
DROP TABLE IF EXISTS social_thread_luck;
DROP TABLE IF EXISTS social_thread_luck_prize;
DROP TABLE IF EXISTS social_thread_luck_winner;
DROP TABLE IF EXISTS social_thread_vote;
DROP TABLE IF EXISTS social_thread_vote_option;
DROP TABLE IF EXISTS social_thread_vote_record;
DROP TABLE IF EXISTS social_thread_vote_submit;
DROP TABLE IF EXISTS social_thread_comment;
DROP TABLE IF EXISTS social_thread_comment_like;
DROP TABLE IF EXISTS social_idle_item;
DROP TABLE IF EXISTS social_thread_collect;
DROP TABLE IF EXISTS social_thread_keep;
DROP TABLE IF EXISTS social_thread_like;
DROP TABLE IF EXISTS social_thread_report;
DROP TABLE IF EXISTS social_thread_share;
DROP TABLE IF EXISTS social_thread_view;
DROP TABLE IF EXISTS social_thread_location;
DROP TABLE IF EXISTS social_thread_topic;
DROP TABLE IF EXISTS social_thread_topic_rel;
DROP TABLE IF EXISTS social_user;
DROP TABLE IF EXISTS core_user_location;

-- core_message_notice.sql
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

-- core_platform_notice.sql
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

-- core_school.sql
CREATE TABLE `core_school`
(
    `id`                bigint NOT NULL COMMENT '主键ID',

    `school_name`       varchar(50)  DEFAULT NULL COMMENT '学校名',
    `simple_name`       varchar(50)  DEFAULT NULL COMMENT '学校简称',
    `school_code`       varchar(255) DEFAULT NULL COMMENT '学校code',
    `school_logo`       varchar(150) DEFAULT NULL COMMENT '学校logo',

    `school_type`       varchar(10)  DEFAULT NULL COMMENT '学校类别（财经类）',
    `build_time`        varchar(20)  DEFAULT NULL COMMENT '建校年份',
    `school_level`      varchar(20)  DEFAULT NULL COMMENT '学校层次（专科、本科）',
    `school_attributes` varchar(20)  DEFAULT NULL COMMENT '学校性质（民办、公办）',
    `school_pro`        json         DEFAULT NULL COMMENT '学校属性（985、211、双一流、强基计划）',
    `college`           json         DEFAULT NULL COMMENT '下属学院',
    `introduction`      text COMMENT '学校介绍',

    `campus_count`      int          DEFAULT NULL COMMENT '下属校区数',
    `initial`           varchar(10)  DEFAULT NULL COMMENT '首字母排序',

    `create_time`       datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_school_code` (`school_code`) USING BTREE,
    FULLTEXT KEY `idx_core_school_school_name_ft` (`school_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='核心学校信息表';

-- core_school_campus.sql
CREATE TABLE `core_school_campus`
(
    `id`           bigint         NOT NULL COMMENT '主键ID',

    `school_code`  varchar(50)  DEFAULT NULL COMMENT '学校编码（关联 core_school.school_code）',
    `campus_code`  varchar(50)  DEFAULT NULL COMMENT '校区编码',
    `campus_name`  varchar(100) DEFAULT NULL COMMENT '校区名字',
    `campus_cover` varchar(150) DEFAULT NULL COMMENT '校区封面图',

    `province`     varchar(100) DEFAULT NULL COMMENT '省份',
    `city`         varchar(100) DEFAULT NULL COMMENT '城市',

    `longitude`    decimal(10, 7) NOT NULL COMMENT '经度(GCJ-02)',
    `latitude`     decimal(10, 7) NOT NULL COMMENT '纬度(GCJ-02)',
    `location`     point          NOT NULL COMMENT '位置信息POINT(经度,纬度)（GCJ-02）',

    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_campus_code` (`campus_code`) USING BTREE,
    SPATIAL KEY `sp_idx_location` (`location`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='核心校区信息表（用于附近搜索，坐标系GCJ-02）';

-- core_user.sql
CREATE TABLE `core_user`
(
    `id`               bigint NOT NULL COMMENT '用户ID',
    `unique_id`        varchar(50)                  DEFAULT NULL COMMENT '用户唯一Id',
    `invite_user_id`   bigint                       DEFAULT NULL COMMENT '邀请人id',

    `union_id`         varchar(255)                 DEFAULT NULL COMMENT '微信用户unionId',

    `avatar_url`       varchar(255)                 DEFAULT NULL COMMENT '头像地址',
    `back_url`         varchar(255)                 DEFAULT NULL COMMENT '背景图像地址',
    `nick_name`        varchar(100)                 DEFAULT NULL COMMENT '昵称',
    `gender`           tinyint                      DEFAULT '0' COMMENT '性别 0:未知，1:男性，2:女性',
    `phone`            varchar(100)                 DEFAULT NULL COMMENT '手机号',
    `birthday`         datetime                     DEFAULT NULL COMMENT '生日',
    `country`          varchar(50)                  DEFAULT NULL COMMENT '国家',
    `province`         varchar(50)                  DEFAULT NULL COMMENT '省份',
    `city`             varchar(50)                  DEFAULT NULL COMMENT '城市',
    `remark`           varchar(255)                 DEFAULT NULL COMMENT '简介',

    `is_disable`       tinyint(1)                   DEFAULT '0' COMMENT '是否禁用：0否，1是',
    `is_destroy`       tinyint(1)                   DEFAULT '0' COMMENT '是否注销：0否，1是',
    `is_fake`          tinyint(1) unsigned zerofill DEFAULT '0' COMMENT '是否虚拟用户：0否，1是',

    `auth_school_code` varchar(100)                 DEFAULT NULL COMMENT '认证学校编码',
    `auth_school_time` datetime                     DEFAULT NULL COMMENT '认证学校时间',
    `auth_phone_time`  datetime                     DEFAULT NULL COMMENT '授权手机号时间',

    `last_login_time`  datetime                     DEFAULT NULL COMMENT '最后登录时间',
    `new_usage_time`   datetime                     DEFAULT NULL COMMENT '最新使用时间',
    `create_time`      datetime                     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime                     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_unique_id` (`unique_id`) USING BTREE,
    UNIQUE KEY `union_id` (`union_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '核心用户表';

-- core_user_default_avatar.sql
CREATE TABLE `core_user_default_avatar`
(
    `id`          bigint       NOT NULL COMMENT '主键ID',
    `avatar_url`  varchar(255) NOT NULL COMMENT '默认头像URL',
    `is_enable`   tinyint(1)   NOT NULL DEFAULT '1' COMMENT '是否启用：0否 1是',
    `weight`      int          NOT NULL DEFAULT '100' COMMENT '权重（用于随机/加权抽取，越大概率越高）',
    `sort`        int          NOT NULL DEFAULT '0' COMMENT '排序（用于固定取值时）',
    `remark`      varchar(255)          DEFAULT NULL COMMENT '备注',

    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_avatar_url` (`avatar_url`) USING BTREE
) ENGINE = InnoDB COMMENT ='新用户默认头像池';

-- core_user_default_nick_name.sql
CREATE TABLE `core_user_default_nick_name`
(
    `id`          bigint       NOT NULL COMMENT '主键ID',
    `nick_name`   varchar(100) NOT NULL COMMENT '默认昵称',
    `is_enable`   tinyint(1)   NOT NULL DEFAULT '1' COMMENT '是否启用：0否 1是',
    `weight`      int          NOT NULL DEFAULT '100' COMMENT '权重（用于随机/加权抽取，越大概率越高）',
    `sort`        int          NOT NULL DEFAULT '0' COMMENT '排序（用于固定取值时）',
    `remark`      varchar(255)          DEFAULT NULL COMMENT '备注',

    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_nick_name` (`nick_name`) USING BTREE
) ENGINE = InnoDB COMMENT ='新用户默认昵称池';

-- edu_school.sql
CREATE TABLE `edu_school`
(
    `id`                  bigint       NOT NULL COMMENT '主键ID',
    `school_code`         varchar(255) NOT NULL COMMENT '学校code（关联 core_school.school_code）',

    `current_term`        varchar(100) DEFAULT NULL COMMENT '当前学期',
    `current_week`        int          DEFAULT NULL COMMENT '当前第几周',
    `start_date`          varchar(100) DEFAULT NULL COMMENT '开学时间',
    `auth_tip`            varchar(150) DEFAULT NULL COMMENT '认证失败提示',

    `status`              tinyint      DEFAULT NULL COMMENT '是否启用：0关闭，1无提示，2已开通，3待开通',
    `expected_open_count` int          DEFAULT NULL COMMENT '总计想要开通人数（仅status=3生效）',
    `open_progress`       int          DEFAULT NULL COMMENT '当前开通进度（百分比，数据库记录的是%前面的数字，仅status=3生效）',
    `open_time`           datetime     DEFAULT NULL COMMENT '开通时间',

    `create_time`         datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_school_code` (`school_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='教务-学校信息（通过school_code关联core_school）';

-- edu_user.sql
CREATE TABLE `edu_user`
(
    `id`          bigint NOT NULL COMMENT 'ID',
    `unique_id`   varchar(50)  DEFAULT NULL COMMENT '用户唯一Id',

    `ma_open_id`  varchar(150) DEFAULT NULL COMMENT '微信小程序openid',
    `mp_open_id`  varchar(150) DEFAULT NULL COMMENT '微信公众号openid',
    `union_id`    varchar(255) DEFAULT NULL COMMENT '微信用户unionId',

    `status`      tinyint      DEFAULT NULL COMMENT '当前是否关注（0 没关注  1 已关注）',

    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `cancel_time` datetime     DEFAULT NULL COMMENT '取消关注时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_ma_open_id` (`ma_open_id`) USING BTREE,
    UNIQUE KEY `idx_mp_open_id` (`mp_open_id`) USING BTREE,
    UNIQUE KEY `idx_union_id` (`union_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '教务用户表';

-- hygiene_user.sql
CREATE TABLE `hygiene_user`
(
    `id`          bigint NOT NULL COMMENT 'ID',
    `unique_id`   varchar(50)  DEFAULT NULL COMMENT '用户唯一Id',

    `ma_open_id`  varchar(150) DEFAULT NULL COMMENT '微信小程序openid',
    `mp_open_id`  varchar(150) DEFAULT NULL COMMENT '微信公众号openid',
    `union_id`    varchar(255) DEFAULT NULL COMMENT '微信用户unionId',

    `status`      tinyint      DEFAULT NULL COMMENT '当前是否关注（0 没关注  1 已关注）',

    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `cancel_time` datetime     DEFAULT NULL COMMENT '取消关注时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_ma_open_id` (`ma_open_id`) USING BTREE,
    UNIQUE KEY `idx_mp_open_id` (`mp_open_id`) USING BTREE,
    UNIQUE KEY `idx_union_id` (`union_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '卫生用户表';

-- social_thread.sql
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

-- social_thread_act_luck.sql
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

-- social_thread_act_luck_prize.sql
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

-- social_thread_act_luck_winner.sql
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

-- social_thread_act_vote.sql
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

-- social_thread_act_vote_option.sql
CREATE TABLE `social_thread_vote_option`
(
    `id`               bigint       NOT NULL COMMENT '主键ID',
    `vote_id`          bigint       NOT NULL COMMENT '投票ID（social_thread_vote.id）',
    `thread_id`        bigint       NOT NULL COMMENT '帖子ID（social_thread.id，冗余便于查询）',

    `option_content`   varchar(200) NOT NULL COMMENT '选项内容',
    `option_desc`      varchar(255)          DEFAULT NULL COMMENT '选项描述（可空）',
    `option_media_url` varchar(255)          DEFAULT NULL COMMENT '选项图片/媒体（可空）',

    `vote_count`       int          NOT NULL DEFAULT '0' COMMENT '选择该选项的人数（缓存计数）',
    `sort`             int          NOT NULL DEFAULT '0' COMMENT '排序（越大越靠前）',
    `is_del`           tinyint(1)   NOT NULL DEFAULT '0' COMMENT '删除标识（0未删除 1已删除）',

    `create_time`      datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子投票选项表';

-- social_thread_act_vote_record.sql
CREATE TABLE `social_thread_vote_record`
(
    `id`           bigint NOT NULL COMMENT '主键ID',

    `vote_id`      bigint NOT NULL COMMENT '投票ID（social_thread_vote.id）',
    `thread_id`    bigint NOT NULL COMMENT '帖子ID（social_thread.id）',
    `option_id`    bigint NOT NULL COMMENT '选项ID（social_thread_vote_option.id）',

    `vote_user_id` bigint NOT NULL COMMENT '投票用户ID',

    `create_time`  datetime DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    `update_time`  datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_vote_user_option` (`vote_id`, `vote_user_id`, `option_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子用户投票记录表';

-- social_thread_act_vote_submit.sql
CREATE TABLE `social_thread_vote_submit`
(
    `id`           bigint NOT NULL COMMENT '主键ID',

    `vote_id`      bigint NOT NULL COMMENT '投票ID（social_thread_vote.id）',
    `thread_id`    bigint NOT NULL COMMENT '帖子ID（social_thread.id）',
    `vote_user_id` bigint NOT NULL COMMENT '投票用户ID',

    `choice_count` int    NOT NULL DEFAULT '0' COMMENT '本次提交选择的选项数',
    `client_ip`    varchar(45)     DEFAULT NULL COMMENT '客户端IP（可选，风控用）',
    `user_agent`   varchar(255)    DEFAULT NULL COMMENT 'UA（可选，风控用）',

    `create_time`  datetime        DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `update_time`  datetime        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_vote_user` (`vote_id`, `vote_user_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子投票提交表（保证一人只能提交一次）';

-- social_thread_comment.sql
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

-- social_thread_comment_like.sql
CREATE TABLE `social_thread_comment_like`
(
    `id`              bigint     NOT NULL COMMENT '主键ID',

    `thread_id`       bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `comment_id`      bigint     NOT NULL COMMENT '评论ID（social_thread_comment.id）',

    `user_id`         bigint     NOT NULL COMMENT '点赞用户ID',
    `comment_user_id` bigint              DEFAULT NULL COMMENT '评论发布者用户ID（冗余，便于通知）',

    `status`          tinyint(1) NOT NULL DEFAULT '1' COMMENT '点赞状态：1点赞 0取消',
    `cancel_time`     datetime            DEFAULT NULL COMMENT '取消时间（status=0时）',

    `create_time`     datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '首次点赞时间',
    `update_time`     datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_comment` (`user_id`, `comment_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='评论点赞表（状态表，一人一评一条）';

-- social_thread_idle_item.sql
CREATE TABLE `social_idle_item`
(
    `id`                bigint         NOT NULL COMMENT '主键ID',
    `thread_id`         bigint         NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint         NOT NULL COMMENT '发布者用户ID（冗余）',
    `school_code`       varchar(50)             DEFAULT NULL COMMENT '学校编码（冗余，可用于校内筛选）',

    `title`             varchar(100)   NOT NULL COMMENT '闲置标题（列表展示/搜索）',
    `category_id`       bigint                  DEFAULT NULL COMMENT '商品类目ID（可选）',
    `brand`             varchar(50)             DEFAULT NULL COMMENT '品牌（可选）',

    `original_price`    decimal(10, 2)          DEFAULT NULL COMMENT '原价',
    `sale_price`        decimal(10, 2) NOT NULL COMMENT '售价',
    `currency`          varchar(10)    NOT NULL DEFAULT 'CNY' COMMENT '币种',

    `condition_level`   tinyint(1)     NOT NULL DEFAULT '0' COMMENT '成色：0未知 1全新 2几乎全新 3轻微使用 4明显使用',
    `trade_mode`        tinyint(1)     NOT NULL DEFAULT '1' COMMENT '交易方式：1当面 2邮寄 3都可',
    `delivery_fee_type` tinyint(1)     NOT NULL DEFAULT '0' COMMENT '运费：0不适用 1包邮 2到付 3自定义',
    `delivery_fee`      decimal(10, 2)          DEFAULT NULL COMMENT '运费金额（delivery_fee_type=3）',

    `stock`             int            NOT NULL DEFAULT '1' COMMENT '数量（默认1）',
    `sale_status`       tinyint(1)     NOT NULL DEFAULT '1' COMMENT '状态：0下架 1在售 2已预订 3已售出',
    `sold_time`         datetime                DEFAULT NULL COMMENT '售出时间',

    -- 用于“附近/最近”检索（GCJ-02）
    `geo_enabled`       tinyint(1)     NOT NULL DEFAULT '0' COMMENT '是否有有效坐标：0否 1是',
    `select_longitude`  decimal(10, 7)          DEFAULT NULL COMMENT '选择位置经度(GCJ-02)',
    `select_latitude`   decimal(10, 7)          DEFAULT NULL COMMENT '选择位置纬度(GCJ-02)',
    `select_location`   point          NOT NULL COMMENT '选择位置POINT(经度,纬度)，无坐标时写POINT(0,0)',

    `create_time`       datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime                DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_thread_id` (`thread_id`) USING BTREE,
    SPATIAL KEY `sp_idx_select_location` (`select_location`)
) ENGINE = InnoDB
    COMMENT ='闲置二手扩展表（与帖子1:1，复用帖子所有互动表）';

-- social_thread_interact_collect.sql
CREATE TABLE `social_thread_collect`
(
    `id`                bigint     NOT NULL COMMENT '主键ID',
    `thread_id`         bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint     NOT NULL COMMENT '收藏用户ID',
    `publisher_user_id` bigint              DEFAULT NULL COMMENT '帖子发布者ID（冗余）',

    `status`            tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1收藏 0取消',
    `cancel_time`       datetime            DEFAULT NULL COMMENT '取消时间（status=0时）',

    `create_time`       datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '首次收藏时间',
    `update_time`       datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_thread` (`user_id`, `thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子收藏表（状态表，一人一贴一条）';

-- social_thread_interact_keep.sql
CREATE TABLE `social_thread_keep`
(
    `id`                bigint     NOT NULL COMMENT '主键ID',
    `thread_id`         bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint     NOT NULL COMMENT '插眼用户ID',
    `publisher_user_id` bigint              DEFAULT NULL COMMENT '帖子发布者ID（冗余）',

    `status`            tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1插眼 0取消',
    `cancel_time`       datetime            DEFAULT NULL COMMENT '取消时间（status=0时）',

    `create_time`       datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '首次插眼时间',
    `update_time`       datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_thread` (`user_id`, `thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子插眼表（状态表，一人一贴一条）';

-- social_thread_interact_like.sql
CREATE TABLE `social_thread_like`
(
    `id`                bigint     NOT NULL COMMENT '主键ID',
    `thread_id`         bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint     NOT NULL COMMENT '点赞用户ID',
    `publisher_user_id` bigint              DEFAULT NULL COMMENT '帖子发布者ID（冗余，便于消息/通知）',

    `status`            tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1点赞 0取消',
    `cancel_time`       datetime            DEFAULT NULL COMMENT '取消时间（status=0时）',

    `create_time`       datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '首次点赞时间',
    `update_time`       datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_thread` (`user_id`, `thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子点赞表（状态表，一人一贴一条）';

-- social_thread_interact_report.sql
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

-- social_thread_interact_share.sql
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

-- social_thread_interact_view.sql
CREATE TABLE `social_thread_view`
(
    `id`                bigint NOT NULL COMMENT '主键ID',
    `thread_id`         bigint NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint NOT NULL COMMENT '浏览用户ID',
    `publisher_user_id` bigint          DEFAULT NULL COMMENT '帖子发布者ID（冗余）',

    `view_count`        int    NOT NULL DEFAULT '1' COMMENT '累计浏览次数（同一用户）',
    `first_view_time`   datetime        DEFAULT CURRENT_TIMESTAMP COMMENT '首次浏览时间',
    `last_view_time`    datetime        DEFAULT CURRENT_TIMESTAMP COMMENT '最近一次浏览时间',

    `create_time`       datetime        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_thread` (`user_id`, `thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子浏览表（聚合表，一人一贴一行）';

-- social_thread_location.sql
CREATE TABLE `social_thread_location`
(
    `id`                bigint     NOT NULL COMMENT '主键ID',

    `thread_id`         bigint     NOT NULL COMMENT '帖子ID（social_thread.id）',
    `user_id`           bigint     NOT NULL COMMENT '发布者用户ID',

    -- 发布时环境信息（风控/校验用）
    `publish_ip`        varchar(45)         DEFAULT NULL COMMENT '发布IP',
    `ip_country`        varchar(50)         DEFAULT NULL COMMENT 'IP解析国家',
    `ip_province`       varchar(100)        DEFAULT NULL COMMENT 'IP解析省份',
    `ip_city`           varchar(100)        DEFAULT NULL COMMENT 'IP解析城市',
    `ip_region`         varchar(100)        DEFAULT NULL COMMENT 'IP解析区/县',
    `ip_adcode`         varchar(20)         DEFAULT NULL COMMENT 'IP解析行政区划码（可选）',

    -- 发布时定位（用户设备定位，可能不展示）
    `publish_longitude` decimal(10, 7)      DEFAULT NULL COMMENT '发布定位经度(GCJ-02)',
    `publish_latitude`  decimal(10, 7)      DEFAULT NULL COMMENT '发布定位纬度(GCJ-02)',
    `publish_location`  point               DEFAULT NULL COMMENT '发布定位POINT(经度,纬度)',

    `publish_address`   varchar(255)        DEFAULT NULL COMMENT '发布地址全称（逆地理结果）',
    `publish_province`  varchar(100)        DEFAULT NULL COMMENT '发布省份',
    `publish_city`      varchar(100)        DEFAULT NULL COMMENT '发布城市',
    `publish_district`  varchar(100)        DEFAULT NULL COMMENT '发布区/县',

    -- 用户选择展示的位置（用于对外展示/附近搜索）
    `select_name`       varchar(255)        DEFAULT NULL COMMENT '用户选择的地点名称/POI',
    `select_longitude`  decimal(10, 7)      DEFAULT NULL COMMENT '选择位置经度(GCJ-02)',
    `select_latitude`   decimal(10, 7)      DEFAULT NULL COMMENT '选择位置纬度(GCJ-02)',
    `select_location`   point               DEFAULT NULL COMMENT '选择位置POINT(经度,纬度)',
    `select_address`    varchar(255)        DEFAULT NULL COMMENT '选择位置全称地址',

    -- 展示与隐私
    `is_show`           tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否展示定位：0否 1是',
    `precision_level`   tinyint(1) NOT NULL DEFAULT '2' COMMENT '展示精度：1省 2市 3区 4街道 5POI',
    `geo_provider`      tinyint(1)          DEFAULT NULL COMMENT '地理服务来源：1高德 2腾讯 3百度 4其他',

    `create_time`       datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       datetime            DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_thread_id` (`thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='帖子位置信息表（发布环境+用户选择位置）';

-- social_thread_topic.sql
CREATE TABLE `social_thread_topic`
(
    `id`              bigint       NOT NULL COMMENT '主键ID',

    `topic_name`      varchar(255) NOT NULL COMMENT '话题名',
    `topic_desc`      varchar(255)          DEFAULT NULL COMMENT '话题描述/简介',
    `background_url`  varchar(255)          DEFAULT NULL COMMENT '话题背景图',

    -- 可见性范围
    `scope_type`      tinyint(1)   NOT NULL DEFAULT '0' COMMENT '可见范围：0全平台 1指定学校可见 2指定学校代理可见',
    `school_code`     varchar(50)           DEFAULT NULL COMMENT '学校编码（scope_type=1/2 时使用）',

    -- 创建与属性
    `creator_user_id` bigint                DEFAULT NULL COMMENT '创建话题用户ID（可为0即官方话题）',
    `is_official`     tinyint(1)   NOT NULL DEFAULT '0' COMMENT '是否官方话题：0否 1是',
    `is_hot`          tinyint(1)   NOT NULL DEFAULT '0' COMMENT '是否热门：0否 1是',
    `hot`             int          NOT NULL DEFAULT '0' COMMENT '热度（用于排序）',

    -- 统计计数（缓存）
    `view_count`      int          NOT NULL DEFAULT '0' COMMENT '阅读量（真实）',
    `comment_count`   int          NOT NULL DEFAULT '0' COMMENT '讨论量（真实）',
    `fake_view`       int          NOT NULL DEFAULT '0' COMMENT '虚拟浏览',
    `fake_comment`    int          NOT NULL DEFAULT '0' COMMENT '虚拟讨论',

    -- 状态控制
    `status`          tinyint(1)   NOT NULL DEFAULT '1' COMMENT '状态：0禁用 1启用',
    `is_del`          tinyint(1)   NOT NULL DEFAULT '0' COMMENT '删除标识：0否 1是',
    `del_time`        datetime              DEFAULT NULL COMMENT '删除时间',

    `create_time`     datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_topic_name_scope` (`topic_name`, `scope_type`, `school_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='社区话题表';

-- social_thread_topic_rel.sql
CREATE TABLE `social_thread_topic_rel`
(
    `topic_id`    bigint NOT NULL COMMENT '话题ID（social_topic.id）',
    `thread_id`   bigint NOT NULL COMMENT '帖子ID（social_thread.id）',

    `school_code` varchar(50) DEFAULT NULL COMMENT '学校编码（冗余，便于按学校筛选话题帖子）',

    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (`topic_id`, `thread_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
    COMMENT ='话题-帖子关联表';

-- social_user.sql
CREATE TABLE `social_user`
(
    `id`          bigint NOT NULL COMMENT 'ID',
    `unique_id`   varchar(50)  DEFAULT NULL COMMENT '用户唯一Id',

    `ma_open_id`  varchar(150) DEFAULT NULL COMMENT '微信小程序openid',
    `mp_open_id`  varchar(150) DEFAULT NULL COMMENT '微信公众号openid',
    `union_id`    varchar(255) DEFAULT NULL COMMENT '微信用户unionId',

    `status`      tinyint      DEFAULT NULL COMMENT '当前是否关注（0 没关注  1 已关注）',

    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `cancel_time` datetime     DEFAULT NULL COMMENT '取消关注时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_ma_open_id` (`ma_open_id`) USING BTREE,
    UNIQUE KEY `idx_mp_open_id` (`mp_open_id`) USING BTREE,
    UNIQUE KEY `idx_union_id` (`union_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '社交用户表';

-- social_user_location.sql
CREATE TABLE `core_user_location`
(
    `id`           bigint NOT NULL COMMENT '主键ID',
    `user_id`      bigint NOT NULL COMMENT '用户ID（core_user.id）',

    -- 原始定位输入
    `ip`           varchar(45)    DEFAULT NULL COMMENT '定位IP（原始，支持IPv4/IPv6）',
    `longitude`    decimal(10, 7) DEFAULT NULL COMMENT '经度（原始）',
    `latitude`     decimal(10, 7) DEFAULT NULL COMMENT '纬度（原始）',
    `accuracy_m`   int            DEFAULT NULL COMMENT '定位精度（米，可选）',

    -- 经纬度逆地理编码结果（由经纬度推断）
    `province`     varchar(100)   DEFAULT NULL COMMENT '省份（逆地理编码）',
    `city`         varchar(100)   DEFAULT NULL COMMENT '城市（逆地理编码）',
    `district`     varchar(100)   DEFAULT NULL COMMENT '区（逆地理编码）',
    `street`       varchar(255)   DEFAULT NULL COMMENT '街道（逆地理编码）',
    `address`      varchar(255)   DEFAULT NULL COMMENT '完整地址（逆地理编码）',

    -- IP 解析属地结果（由IP推断）
    `ip_region`    varchar(255)   DEFAULT NULL COMMENT 'IP属地（文本，如：浙江省·杭州市）',
    `ip_province`  varchar(100)   DEFAULT NULL COMMENT 'IP属地-省（可选，便于统计）',
    `ip_city`      varchar(100)   DEFAULT NULL COMMENT 'IP属地-市（可选，便于统计）',

    -- 解析/来源元数据（可追溯）
    `loc_source`   tinyint        DEFAULT '0' COMMENT '定位来源：0未知/1GPS/2基站/3WiFi/4IP/5手动',
    `geo_provider` varchar(50)    DEFAULT 'Tencent' COMMENT '逆地理/属地解析服务商（如Amap/Tencent/Baidu/自建）',
    `geo_time`     datetime       DEFAULT NULL COMMENT '最近一次完成解析的时间',

    `create_time`  datetime       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime       DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
    COMMENT ='核心用户定位表（原始定位 + 逆地理编码 + IP属地）';

-- core_school_campus-触发器.sql
--  写入时自动生成 location

DELIMITER $$

CREATE TRIGGER `tr_core_school_campus_bi_location`
    BEFORE INSERT
    ON `core_school_campus`
    FOR EACH ROW
BEGIN
    -- 经纬度必须有值（否则无法生成 location）
    IF NEW.longitude IS NULL OR NEW.latitude IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经度和纬度不能为空';
    END IF;

    -- 基本范围校验（经度[-180,180] 纬度[-90,90]）
    IF NEW.longitude < -180 OR NEW.longitude > 180
        OR NEW.latitude < -90 OR NEW.latitude > 90 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经纬度超出有效范围（经度[-180,180]，纬度[-90,90]）';
    END IF;

    -- 禁止手工写 location：永远以经纬度生成
    SET NEW.location = POINT(NEW.longitude, NEW.latitude);
END$$


CREATE TRIGGER `tr_core_school_campus_bu_location`
    BEFORE UPDATE
    ON `core_school_campus`
    FOR EACH ROW
BEGIN
    -- 经纬度必须有值（否则无法生成 location）
    IF NEW.longitude IS NULL OR NEW.latitude IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经度和纬度不能为空';
    END IF;

    -- 基本范围校验（经度[-180,180] 纬度[-90,90]）
    IF NEW.longitude < -180 OR NEW.longitude > 180
        OR NEW.latitude < -90 OR NEW.latitude > 90 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经纬度超出有效范围（经度[-180,180]，纬度[-90,90]）';
    END IF;

    -- 无论用户是否手工修改 location，都强制覆盖：永远以经纬度为准
    SET NEW.location = POINT(NEW.longitude, NEW.latitude);
END$$

DELIMITER ;

-- social_thread_idle_item-触发器.sql
DROP TRIGGER IF EXISTS tr_social_idle_item_bi_geo;
DROP TRIGGER IF EXISTS tr_social_idle_item_bu_geo;

DELIMITER $$

CREATE TRIGGER tr_social_idle_item_bi_geo
    BEFORE INSERT
    ON social_idle_item
    FOR EACH ROW
BEGIN
    -- 经度纬度必须成对出现
    IF (NEW.select_longitude IS NULL) <> (NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经度和纬度必须同时为空或同时有值';
    END IF;

    -- 范围校验（可选但推荐）
    IF NEW.select_longitude IS NOT NULL AND (NEW.select_longitude < -180 OR NEW.select_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '经度超出有效范围[-180,180]';
    END IF;
    IF NEW.select_latitude IS NOT NULL AND (NEW.select_latitude < -90 OR NEW.select_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '纬度超出有效范围[-90,90]';
    END IF;

    -- 强制由经纬度生成 location；未提供坐标则写 POINT(0,0)，并标记 geo_enabled=0
    IF NEW.select_longitude IS NULL THEN
        SET NEW.geo_enabled = 0;
        SET NEW.select_location = POINT(0, 0);
    ELSE
        SET NEW.geo_enabled = 1;
        SET NEW.select_location = POINT(NEW.select_longitude, NEW.select_latitude);
    END IF;
END$$

CREATE TRIGGER tr_social_idle_item_bu_geo
    BEFORE UPDATE
    ON social_idle_item
    FOR EACH ROW
BEGIN
    IF (NEW.select_longitude IS NULL) <> (NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '经度和纬度必须同时为空或同时有值';
    END IF;

    IF NEW.select_longitude IS NOT NULL AND (NEW.select_longitude < -180 OR NEW.select_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '经度超出有效范围[-180,180]';
    END IF;
    IF NEW.select_latitude IS NOT NULL AND (NEW.select_latitude < -90 OR NEW.select_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '纬度超出有效范围[-90,90]';
    END IF;

    -- 禁止手工写 location：永远覆盖
    IF NEW.select_longitude IS NULL THEN
        SET NEW.geo_enabled = 0;
        SET NEW.select_location = POINT(0, 0);
    ELSE
        SET NEW.geo_enabled = 1;
        SET NEW.select_location = POINT(NEW.select_longitude, NEW.select_latitude);
    END IF;
END$$

DELIMITER ;

-- social_thread_location-触发器.sql
DROP TRIGGER IF EXISTS tr_social_thread_location_bi_point;
DROP TRIGGER IF EXISTS tr_social_thread_location_bu_point;

DELIMITER $$

CREATE TRIGGER tr_social_thread_location_bi_point
    BEFORE INSERT
    ON social_thread_location
    FOR EACH ROW
BEGIN
    -- 1) 经度纬度必须成对出现（publish）
    IF (NEW.publish_longitude IS NULL) <> (NEW.publish_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '发布经度和纬度必须同时为空或同时有值';
    END IF;

    -- 2) 经度纬度必须成对出现（select）
    IF (NEW.select_longitude IS NULL) <> (NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '选择经度和纬度必须同时为空或同时有值';
    END IF;

    -- 3) 如果要展示定位，必须提供 select 经纬度
    IF NEW.is_show = 1 AND (NEW.select_longitude IS NULL OR NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '展示定位时必须提供选择的经纬度';
    END IF;

    -- 4) 范围校验（可选但推荐）
    IF NEW.publish_longitude IS NOT NULL AND (NEW.publish_longitude < -180 OR NEW.publish_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '发布经度超出有效范围[-180,180]';
    END IF;
    IF NEW.publish_latitude IS NOT NULL AND (NEW.publish_latitude < -90 OR NEW.publish_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '发布纬度超出有效范围[-90,90]';
    END IF;

    IF NEW.select_longitude IS NOT NULL AND (NEW.select_longitude < -180 OR NEW.select_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '选择经度超出有效范围[-180,180]';
    END IF;
    IF NEW.select_latitude IS NOT NULL AND (NEW.select_latitude < -90 OR NEW.select_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '选择纬度超出有效范围[-90,90]';
    END IF;

    -- 5) 禁止手工写 POINT：永远由经纬度生成
    IF NEW.publish_longitude IS NULL THEN
        SET NEW.publish_location = NULL;
    ELSE
        SET NEW.publish_location = POINT(NEW.publish_longitude, NEW.publish_latitude);
    END IF;

    IF NEW.select_longitude IS NULL THEN
        SET NEW.select_location = NULL;
    ELSE
        SET NEW.select_location = POINT(NEW.select_longitude, NEW.select_latitude);
    END IF;
END$$


CREATE TRIGGER tr_social_thread_location_bu_point
    BEFORE UPDATE
    ON social_thread_location
    FOR EACH ROW
BEGIN
    -- 1) 经度纬度必须成对出现（publish）
    IF (NEW.publish_longitude IS NULL) <> (NEW.publish_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '发布经度和纬度必须同时为空或同时有值';
    END IF;

    -- 2) 经度纬度必须成对出现（select）
    IF (NEW.select_longitude IS NULL) <> (NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '选择经度和纬度必须同时为空或同时有值';
    END IF;

    -- 3) 如果要展示定位，必须提供 select 经纬度
    IF NEW.is_show = 1 AND (NEW.select_longitude IS NULL OR NEW.select_latitude IS NULL) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = '展示定位时必须提供选择的经纬度';
    END IF;

    -- 4) 范围校验
    IF NEW.publish_longitude IS NOT NULL AND (NEW.publish_longitude < -180 OR NEW.publish_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '发布经度超出有效范围[-180,180]';
    END IF;
    IF NEW.publish_latitude IS NOT NULL AND (NEW.publish_latitude < -90 OR NEW.publish_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '发布纬度超出有效范围[-90,90]';
    END IF;

    IF NEW.select_longitude IS NOT NULL AND (NEW.select_longitude < -180 OR NEW.select_longitude > 180) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '选择经度超出有效范围[-180,180]';
    END IF;
    IF NEW.select_latitude IS NOT NULL AND (NEW.select_latitude < -90 OR NEW.select_latitude > 90) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '选择纬度超出有效范围[-90,90]';
    END IF;

    -- 5) 强制覆盖 POINT（禁止手工改 POINT）
    IF NEW.publish_longitude IS NULL THEN
        SET NEW.publish_location = NULL;
    ELSE
        SET NEW.publish_location = POINT(NEW.publish_longitude, NEW.publish_latitude);
    END IF;

    IF NEW.select_longitude IS NULL THEN
        SET NEW.select_location = NULL;
    ELSE
        SET NEW.select_location = POINT(NEW.select_longitude, NEW.select_latitude);
    END IF;
END$$

DELIMITER ;
