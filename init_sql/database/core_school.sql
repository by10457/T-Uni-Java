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
