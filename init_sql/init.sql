CREATE DATABASE IF NOT EXISTS tuni DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE tuni;

DROP TABLE IF EXISTS biz_user;
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

INSERT INTO `core_user_default_nick_name` (`id`, `nick_name`, `is_enable`, `weight`, `sort`, `remark`)
VALUES (1, '星河旅人', 1, 100, 10, '默认昵称示例 1'),
       (2, '晨光信使', 1, 100, 20, '默认昵称示例 2');
