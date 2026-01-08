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
    