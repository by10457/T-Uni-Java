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
