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
