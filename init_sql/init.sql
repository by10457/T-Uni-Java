SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS tuni DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE tuni;

DROP TABLE IF EXISTS biz_user;
DROP TABLE IF EXISTS core_payment_notify_log;
DROP TABLE IF EXISTS core_payment_refund;
DROP TABLE IF EXISTS core_payment_transaction;
DROP TABLE IF EXISTS core_payment_order;
DROP TABLE IF EXISTS core_user_default_nick_name;
DROP TABLE IF EXISTS core_user_default_avatar;
DROP TABLE IF EXISTS core_user;

CREATE TABLE `core_user`
(
    `id`               bigint NOT NULL COMMENT '用户ID',
    `unique_id`        varchar(50)                  DEFAULT NULL COMMENT '用户唯一ID',
    `invite_user_id`   bigint                       DEFAULT NULL COMMENT '邀请人ID',
    `union_id`         varchar(255)                 DEFAULT NULL COMMENT '微信用户 unionId',
    `avatar_url`       varchar(255)                 DEFAULT NULL COMMENT '头像地址',
    `back_url`         varchar(255)                 DEFAULT NULL COMMENT '背景图地址',
    `nick_name`        varchar(100)                 DEFAULT NULL COMMENT '昵称',
    `gender`           tinyint                      DEFAULT '0' COMMENT '性别 0未知 1男性 2女性',
    `phone`            varchar(100)                 DEFAULT NULL COMMENT '手机号',
    `birthday`         datetime                     DEFAULT NULL COMMENT '生日',
    `country`          varchar(50)                  DEFAULT NULL COMMENT '国家',
    `province`         varchar(50)                  DEFAULT NULL COMMENT '省份',
    `city`             varchar(50)                  DEFAULT NULL COMMENT '城市',
    `remark`           varchar(255)                 DEFAULT NULL COMMENT '简介',
    `is_disable`       tinyint(1)                   DEFAULT '0' COMMENT '是否禁用：0否 1是',
    `is_destroy`       tinyint(1)                   DEFAULT '0' COMMENT '是否注销：0否 1是',
    `is_fake`          tinyint(1) unsigned zerofill DEFAULT '0' COMMENT '是否虚拟用户：0否 1是',
    `im_registered`    tinyint(1)                   DEFAULT '0' COMMENT '是否已同步到OpenIM：0否 1是',
    `auth_school_code` varchar(100)                 DEFAULT NULL COMMENT '认证学校编码',
    `auth_school_time` datetime                     DEFAULT NULL COMMENT '认证学校时间',
    `auth_phone_time`  datetime                     DEFAULT NULL COMMENT '授权手机号时间',
    `last_login_time`  datetime                     DEFAULT NULL COMMENT '最后登录时间',
    `new_usage_time`   datetime                     DEFAULT NULL COMMENT '最近使用时间',
    `create_time`      datetime                     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime                     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_core_user_unique_id` (`unique_id`) USING BTREE,
    UNIQUE KEY `uk_core_user_union_id` (`union_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '核心用户表';

CREATE TABLE `core_user_default_avatar`
(
    `id`          bigint       NOT NULL COMMENT '主键ID',
    `avatar_url`  varchar(255) NOT NULL COMMENT '默认头像URL',
    `is_enable`   tinyint(1)   NOT NULL DEFAULT '1' COMMENT '是否启用：0否 1是',
    `weight`      int          NOT NULL DEFAULT '100' COMMENT '权重（用于随机抽取）',
    `sort`        int          NOT NULL DEFAULT '0' COMMENT '排序',
    `remark`      varchar(255)          DEFAULT NULL COMMENT '备注',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_avatar_url` (`avatar_url`) USING BTREE
) ENGINE = InnoDB COMMENT ='默认头像池';

CREATE TABLE `core_user_default_nick_name`
(
    `id`          bigint       NOT NULL COMMENT '主键ID',
    `nick_name`   varchar(100) NOT NULL COMMENT '默认昵称',
    `is_enable`   tinyint(1)   NOT NULL DEFAULT '1' COMMENT '是否启用：0否 1是',
    `weight`      int          NOT NULL DEFAULT '100' COMMENT '权重（用于随机抽取）',
    `sort`        int          NOT NULL DEFAULT '0' COMMENT '排序',
    `remark`      varchar(255)          DEFAULT NULL COMMENT '备注',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_nick_name` (`nick_name`) USING BTREE
) ENGINE = InnoDB COMMENT ='默认昵称池';

CREATE TABLE `biz_user`
(
    `id`          bigint NOT NULL COMMENT 'ID',
    `unique_id`   varchar(50)  DEFAULT NULL COMMENT '用户唯一ID',
    `ma_open_id`  varchar(150) DEFAULT NULL COMMENT '微信小程序 openId',
    `mp_open_id`  varchar(150) DEFAULT NULL COMMENT '微信公众号 openId',
    `union_id`    varchar(255) DEFAULT NULL COMMENT '微信用户 unionId',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_biz_user_ma_open_id` (`ma_open_id`) USING BTREE,
    UNIQUE KEY `uk_biz_user_mp_open_id` (`mp_open_id`) USING BTREE,
    UNIQUE KEY `uk_biz_user_union_id` (`union_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '模板默认业务用户表';

CREATE TABLE `core_payment_order`
(
    `id`             bigint      NOT NULL COMMENT '主键ID',
    `biz_type`       varchar(32) NOT NULL COMMENT '业务类型，如 AI_PAPER/VIP/COURSE',
    `biz_order_no`   varchar(64) NOT NULL COMMENT '业务订单号',
    `order_no`       varchar(64) NOT NULL COMMENT '商户支付单号 out_trade_no',
    `user_id`        bigint      NOT NULL COMMENT '用户ID',
    `description`    varchar(128)         DEFAULT NULL COMMENT '微信支付商品描述',
    `total_fee_fen`  int         NOT NULL COMMENT '支付金额，单位分',
    `refund_fee_fen` int         NOT NULL DEFAULT '0' COMMENT '累计已退款金额，单位分',
    `currency`       varchar(8)  NOT NULL DEFAULT 'CNY' COMMENT '币种',
    `pay_channel`    tinyint     NOT NULL DEFAULT '1' COMMENT '支付渠道：1微信',
    `status`         tinyint     NOT NULL DEFAULT '0' COMMENT '状态：0已创建 1预下单成功 2已支付 3已关闭 4退款中 5已全额退款 6支付失败 7已部分退款',
    `expire_time`    datetime             DEFAULT NULL COMMENT '支付过期时间',
    `paid_time`      datetime             DEFAULT NULL COMMENT '支付成功时间',
    `close_time`     datetime             DEFAULT NULL COMMENT '关闭时间',
    `attach_data`    varchar(512)         DEFAULT NULL COMMENT '附加数据',
    `create_time`    datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime             DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_payment_order_no` (`order_no`) USING BTREE,
    UNIQUE KEY `uk_payment_biz_order` (`biz_type`, `biz_order_no`) USING BTREE,
    KEY `idx_payment_user_status_time` (`user_id`, `status`, `create_time`) USING BTREE,
    KEY `idx_payment_expire` (`status`, `expire_time`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付主订单';

CREATE TABLE `core_payment_transaction`
(
    `id`             bigint      NOT NULL COMMENT '主键ID',
    `order_id`       bigint      NOT NULL COMMENT '支付主订单ID',
    `out_trade_no`   varchar(64) NOT NULL COMMENT '商户支付单号',
    `biz_order_no`   varchar(64) NOT NULL COMMENT '业务订单号',
    `user_id`        bigint      NOT NULL COMMENT '用户ID',
    `pay_channel`    tinyint     NOT NULL DEFAULT '1' COMMENT '支付渠道：1微信',
    `trade_type`     varchar(16) NOT NULL DEFAULT 'JSAPI' COMMENT '交易类型',
    `status`         tinyint     NOT NULL DEFAULT '0' COMMENT '状态：0初始化 1预下单成功 2支付成功 3支付失败 4已关闭 5已退款',
    `total_fee_fen`  int         NOT NULL COMMENT '支付金额，单位分',
    `transaction_id` varchar(64)          DEFAULT NULL COMMENT '微信支付订单号',
    `prepay_id`      varchar(128)         DEFAULT NULL COMMENT '微信预支付ID',
    `notify_time`    datetime             DEFAULT NULL COMMENT '回调通知时间',
    `success_time`   datetime             DEFAULT NULL COMMENT '支付成功时间',
    `fail_reason`    varchar(255)         DEFAULT NULL COMMENT '失败原因',
    `raw_response`   text COMMENT '微信原始响应',
    `create_time`    datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime             DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_payment_tx_out_trade_no` (`out_trade_no`) USING BTREE,
    UNIQUE KEY `uk_payment_tx_transaction_id` (`transaction_id`) USING BTREE,
    KEY `idx_payment_tx_order_id` (`order_id`) USING BTREE,
    KEY `idx_payment_tx_user_status_time` (`user_id`, `status`, `create_time`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付交易流水';

CREATE TABLE `core_payment_refund`
(
    `id`             bigint      NOT NULL COMMENT '主键ID',
    `order_id`       bigint      NOT NULL COMMENT '支付主订单ID',
    `out_trade_no`   varchar(64) NOT NULL COMMENT '原商户支付单号',
    `out_refund_no`  varchar(64) NOT NULL COMMENT '商户退款单号',
    `user_id`        bigint      NOT NULL COMMENT '用户ID',
    `refund_fee_fen` int         NOT NULL COMMENT '本次退款金额，单位分',
    `refund_reason`  varchar(255)         DEFAULT NULL COMMENT '退款原因',
    `status`         tinyint     NOT NULL DEFAULT '0' COMMENT '状态：0申请中 1处理中 2成功 3失败/异常 4关闭',
    `refund_id`      varchar(64)          DEFAULT NULL COMMENT '微信退款单号',
    `notify_time`    datetime             DEFAULT NULL COMMENT '回调通知时间',
    `success_time`   datetime             DEFAULT NULL COMMENT '退款成功时间',
    `fail_reason`    varchar(255)         DEFAULT NULL COMMENT '失败原因',
    `raw_response`   text COMMENT '微信原始响应',
    `create_time`    datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime             DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_payment_refund_no` (`out_refund_no`) USING BTREE,
    UNIQUE KEY `uk_payment_refund_id` (`refund_id`) USING BTREE,
    KEY `idx_payment_refund_order_id` (`order_id`) USING BTREE,
    KEY `idx_payment_refund_user_status_time` (`user_id`, `status`, `create_time`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付退款单';

CREATE TABLE `core_payment_notify_log`
(
    `id`             bigint       NOT NULL COMMENT '主键ID',
    `notify_type`    tinyint      NOT NULL COMMENT '通知类型：1支付 2退款',
    `notify_id`      varchar(128) NOT NULL COMMENT '微信通知ID',
    `out_trade_no`   varchar(64)           DEFAULT NULL COMMENT '商户支付单号',
    `transaction_id` varchar(64)           DEFAULT NULL COMMENT '微信支付订单号',
    `out_refund_no`  varchar(64)           DEFAULT NULL COMMENT '商户退款单号',
    `refund_id`      varchar(64)           DEFAULT NULL COMMENT '微信退款单号',
    `headers_json`   text COMMENT '回调请求头JSON',
    `raw_body`       longtext COMMENT '原始回调body',
    `process_status` tinyint      NOT NULL DEFAULT '0' COMMENT '处理状态：0已接收 1成功 2忽略 3失败',
    `error_msg`      varchar(500)          DEFAULT NULL COMMENT '错误信息',
    `create_time`    datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime              DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_payment_notify` (`notify_type`, `notify_id`) USING BTREE,
    KEY `idx_payment_notify_out_trade_no` (`out_trade_no`) USING BTREE,
    KEY `idx_payment_notify_out_refund_no` (`out_refund_no`) USING BTREE,
    KEY `idx_payment_notify_status_time` (`process_status`, `create_time`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '支付回调日志';

INSERT INTO `core_user_default_nick_name` (`id`, `nick_name`, `is_enable`, `weight`, `sort`, `remark`)
VALUES (1, '星河旅人', 1, 100, 10, '默认昵称示例 1'),
       (2, '晨光信使', 1, 100, 20, '默认昵称示例 2');
