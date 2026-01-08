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