/*
 Navicat Premium Dump SQL

 Source Server         : 阿里云
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : rm-bp12z6hlv46vi6g8mro.mysql.rds.aliyuncs.com:3306
 Source Schema         : t_uni

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 09/05/2025 19:58:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for log_quartz_execute
-- ----------------------------
DROP TABLE IF EXISTS `log_quartz_execute`;
CREATE TABLE `log_quartz_execute`
(
    `id`              bigint                                                        NOT NULL COMMENT '主键',
    `job_name`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务名称',
    `job_group`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务分组',
    `job_class_name`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行任务类名',
    `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行任务core表达式',
    `trigger_name`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '触发器名称',
    `execute_result`  json                                                          NOT NULL COMMENT '执行结果',
    `duration`        int                                                           NULL     DEFAULT NULL COMMENT '执行时间',
    `create_time`     datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改的时',
    `create_user`     bigint                                                        NULL     DEFAULT NULL COMMENT '创建用户',
    `update_user`     bigint                                                        NULL     DEFAULT NULL COMMENT '操作用户',
    `is_deleted`      tinyint(1) UNSIGNED ZEROFILL                                  NOT NULL DEFAULT 0 COMMENT '文件是否被删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_job_name` (`job_name` ASC) USING BTREE COMMENT '索引执行任务名称',
    INDEX `idx_job_group` (`job_group` ASC) USING BTREE COMMENT '索引执行任务分组',
    INDEX `idx_job_class_name` (`job_class_name` ASC) USING BTREE COMMENT '索引执行任务类名',
    INDEX `idx_trigger_name` (`trigger_name` ASC) USING BTREE COMMENT '索引执行任务触发器名称',
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '调度任务执行日志'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of log_quartz_execute
-- ----------------------------

-- ----------------------------
-- Table structure for log_user_login
-- ----------------------------
DROP TABLE IF EXISTS `log_user_login`;
CREATE TABLE `log_user_login`
(
    `id`               bigint                                                        NOT NULL COMMENT '主键',
    `user_id`          bigint                                                        NULL     DEFAULT NULL COMMENT '用户Id',
    `username`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '用户名',
    `token`            text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NULL COMMENT '登录token',
    `ip_address`       varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '登录Ip',
    `ip_region`        varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '登录Ip归属地',
    `user_agent`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '登录时代理',
    `type`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '操作类型',
    `x_requested_with` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '标识客户端是否是通过Ajax发送请求的',
    `create_time`      datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_user`      bigint                                                        NOT NULL COMMENT '创建用户',
    `update_user`      bigint                                                        NULL     DEFAULT NULL COMMENT '操作用户',
    `is_deleted`       tinyint(1) UNSIGNED ZEROFILL                                  NOT NULL DEFAULT 0 COMMENT '是否被删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_username` (`username` ASC) USING BTREE,
    INDEX `idx_ip_address` (`ip_address` ASC) USING BTREE,
    INDEX `idx_ip_region` (`ip_region` ASC) USING BTREE,
    INDEX `idx_type` (`type` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE,
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE,
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '用户登录日志'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of log_user_login
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers`
(
    `sched_name`    varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '固定名称',
    `trigger_name`  varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器名称',
    `trigger_group` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器分组',
    `blob_data`     blob                                                          NULL COMMENT '二进制时间',
    PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
    INDEX `SCHED_NAME` (`sched_name` ASC, `trigger_name` ASC, `trigger_group` ASC) USING BTREE,
    CONSTRAINT `QRTZ_BLOB_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '二进制触发器表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`
(
    `sched_name`    varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '固定名称',
    `calendar_name` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '日历名称',
    `calendar`      blob                                                          NOT NULL COMMENT '日历',
    PRIMARY KEY (`sched_name`, `calendar_name`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '日历表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers`
(
    `sched_name`      varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '固定名称',
    `trigger_name`    varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器名称',
    `trigger_group`   varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器分组',
    `cron_expression` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'cron表达式',
    `time_zone_id`    varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '时区id',
    PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
    CONSTRAINT `QRTZ_CRON_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'corn表达式触发器表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------
INSERT INTO `qrtz_cron_triggers`
VALUES ('quartzScheduler', 'test', 'hello分组', '0/5 * * * * ?', 'Asia/Shanghai');

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`
(
    `sched_name`        varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '固定名称',
    `entry_id`          varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '固定名称',
    `trigger_name`      varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '固定名称',
    `trigger_group`     varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '固定名称',
    `instance_name`     varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '固定名称',
    `fired_time`        bigint                                                        NOT NULL COMMENT '固定名称',
    `sched_time`        bigint                                                        NOT NULL COMMENT '固定名称',
    `priority`          int                                                           NOT NULL COMMENT '固定名称',
    `state`             varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '固定名称',
    `job_name`          varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '固定名称',
    `job_group`         varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '固定名称',
    `is_nonconcurrent`  varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT NULL COMMENT '固定名称',
    `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT NULL COMMENT '固定名称',
    PRIMARY KEY (`sched_name`, `entry_id`) USING BTREE,
    INDEX `IDX_QRTZ_FT_TRIG_INST_NAME` (`sched_name` ASC, `instance_name` ASC) USING BTREE,
    INDEX `IDX_QRTZ_FT_INST_JOB_REQ_RCVRY` (`sched_name` ASC, `instance_name` ASC, `requests_recovery` ASC) USING BTREE,
    INDEX `IDX_QRTZ_FT_J_G` (`sched_name` ASC, `job_name` ASC, `job_group` ASC) USING BTREE,
    INDEX `IDX_QRTZ_FT_JG` (`sched_name` ASC, `job_group` ASC) USING BTREE,
    INDEX `IDX_QRTZ_FT_T_G` (`sched_name` ASC, `trigger_name` ASC, `trigger_group` ASC) USING BTREE,
    INDEX `IDX_QRTZ_FT_TG` (`sched_name` ASC, `trigger_group` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '解除触发器'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details`
(
    `sched_name`        varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度器名称',
    `job_name`          varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
    `job_group`         varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务组名称',
    `description`       varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '任务描述',
    `job_class_name`    varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务类名称',
    `is_durable`        varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NOT NULL COMMENT '是否持久化',
    `is_nonconcurrent`  varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NOT NULL COMMENT '是否非并发执行',
    `is_update_data`    varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NOT NULL COMMENT '是否更新数据',
    `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NOT NULL COMMENT '是否请求恢复',
    `job_data`          blob                                                          NULL,
    PRIMARY KEY (`sched_name`, `job_name`, `job_group`) USING BTREE,
    INDEX `idx_qrtz_j_req_recovery` (`sched_name` ASC, `requests_recovery` ASC) USING BTREE,
    INDEX `idx_qrtz_j_grp` (`sched_name` ASC, `job_group` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务详情表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------
INSERT INTO `qrtz_job_details`
VALUES ('quartzScheduler', 'test', 'hello分组', 'test', 't.uni.services.quartz.JobHello', '0', '0', '0', '0',
        0xACED0005737200156F72672E71756172747A2E4A6F62446174614D61709FB083E8BFA9B0CB020000787200266F72672E71756172747A2E7574696C732E537472696E674B65794469727479466C61674D61708208E8C3FBC55D280200015A0013616C6C6F77735472616E7369656E74446174617872001D6F72672E71756172747A2E7574696C732E4469727479466C61674D617013E62EAD28760ACE0200025A000564697274794C00036D617074000F4C6A6176612F7574696C2F4D61703B787001737200116A6176612E7574696C2E486173684D61700507DAC1C31660D103000246000A6C6F6164466163746F724900097468726573686F6C6478703F4000000000000C770800000010000000047400076A6F624E616D657400047465737474000E63726F6E45787072657373696F6E74000D302F31202A202A202A202A203F74000B747269676765724E616D6571007E00087400086A6F6247726F757074000B68656C6C6FE58886E7BB847800);

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`
(
    `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度器名称',
    `lock_name`  varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '锁名称',
    PRIMARY KEY (`sched_name`, `lock_name`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = 'quartz锁'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------
INSERT INTO `qrtz_locks`
VALUES ('quartzScheduler', 'TRIGGER_ACCESS');

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`
(
    `sched_name`    varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度器名称',
    `trigger_group` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器组名',
    PRIMARY KEY (`sched_name`, `trigger_group`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '暂停触发器分组'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`
(
    `sched_name`        varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度器名称',
    `instance_name`     varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实例名称',
    `last_checkin_time` bigint                                                        NOT NULL COMMENT '最后检查时间',
    `checkin_interval`  bigint                                                        NOT NULL COMMENT '检查间隔',
    PRIMARY KEY (`sched_name`, `instance_name`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '调度器状态表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_schedulers_group
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_schedulers_group`;
CREATE TABLE `qrtz_schedulers_group`
(
    `id`          bigint                                                        NOT NULL COMMENT '唯一id',
    `group_name`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分组名称',
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '分组详情',
    `create_time` datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                                                        NOT NULL COMMENT '创建用户',
    `update_user` bigint                                                        NULL     DEFAULT NULL COMMENT '操作用户',
    `is_deleted`  tinyint(1) UNSIGNED ZEROFILL                                  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_group_name` (`group_name` ASC) USING BTREE COMMENT '分组名称需要唯一',
    INDEX `idx_description` (`description` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务调度分组表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_schedulers_group
-- ----------------------------
INSERT INTO `qrtz_schedulers_group`
VALUES (1846167458097774594, 'hello分组', 'hello分组作为第一个测试', '2024-10-15 20:33:17', '2024-10-15 20:33:17', 1, 1,
        0);

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers`
(
    `sched_name`      varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度器名称',
    `trigger_name`    varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器名称',
    `trigger_group`   varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器组',
    `repeat_count`    bigint                                                        NOT NULL COMMENT '重复次数',
    `repeat_interval` bigint                                                        NOT NULL COMMENT '重复间隔',
    `times_triggered` bigint                                                        NOT NULL COMMENT '触发次数',
    PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
    CONSTRAINT `QRTZ_SIMPLE_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '简单触发器表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers`
(
    `sched_name`    varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
    `trigger_name`  varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器名称',
    `trigger_group` varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器组',
    `str_prop_1`    varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字符串属性',
    `str_prop_2`    varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字符串属性',
    `str_prop_3`    varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字符串属性',
    `int_prop_1`    int                                                           NULL DEFAULT NULL COMMENT '整型属性',
    `int_prop_2`    int                                                           NULL DEFAULT NULL COMMENT '整型属性',
    `long_prop_1`   bigint                                                        NULL DEFAULT NULL COMMENT '长整型属性',
    `long_prop_2`   bigint                                                        NULL DEFAULT NULL COMMENT '长整型属性',
    `dec_prop_1`    decimal(13, 4)                                                NULL DEFAULT NULL COMMENT '十进制属性',
    `dec_prop_2`    decimal(13, 4)                                                NULL DEFAULT NULL COMMENT '十进制属性',
    `bool_prop_1`   varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT NULL COMMENT '布尔型属性',
    `bool_prop_2`   varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT NULL COMMENT '布尔型属性',
    PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
    CONSTRAINT `QRTZ_SIMPROP_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '简单操作触发器'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers`
(
    `sched_name`     varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
    `trigger_name`   varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器名称',
    `trigger_group`  varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器组',
    `job_name`       varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业名称',
    `job_group`      varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作业组',
    `description`    varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
    `next_fire_time` bigint                                                        NULL DEFAULT NULL COMMENT '触发时间',
    `prev_fire_time` bigint                                                        NULL DEFAULT NULL COMMENT '触发时间',
    `priority`       int                                                           NULL DEFAULT NULL COMMENT '优先级',
    `trigger_state`  varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '触发器状态',
    `trigger_type`   varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NOT NULL COMMENT '触发器类型',
    `start_time`     bigint                                                        NOT NULL COMMENT '开始时间',
    `end_time`       bigint                                                        NULL DEFAULT NULL COMMENT '结束时间',
    `calendar_name`  varchar(190) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '日历名称',
    `misfire_instr`  smallint                                                      NULL DEFAULT NULL COMMENT '错过触发指令',
    `job_data`       blob                                                          NULL,
    PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
    INDEX `IDX_QRTZ_T_J` (`sched_name` ASC, `job_name` ASC, `job_group` ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_JG` (`sched_name` ASC, `job_group` ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_C` (`sched_name` ASC, `calendar_name` ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_G` (`sched_name` ASC, `trigger_group` ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_STATE` (`sched_name` ASC, `trigger_state` ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_N_STATE` (`sched_name` ASC, `trigger_name` ASC, `trigger_group` ASC, `trigger_state`
                                ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_N_G_STATE` (`sched_name` ASC, `trigger_group` ASC, `trigger_state` ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_NEXT_FIRE_TIME` (`sched_name` ASC, `next_fire_time` ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_NFT_ST` (`sched_name` ASC, `trigger_state` ASC, `next_fire_time` ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_NFT_MISFIRE` (`sched_name` ASC, `misfire_instr` ASC, `next_fire_time` ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_NFT_ST_MISFIRE` (`sched_name` ASC, `misfire_instr` ASC, `next_fire_time` ASC, `trigger_state`
                                       ASC) USING BTREE,
    INDEX `IDX_QRTZ_T_NFT_ST_MISFIRE_GRP` (`sched_name` ASC, `misfire_instr` ASC, `next_fire_time` ASC, `trigger_group`
                                           ASC, `trigger_state` ASC) USING BTREE,
    CONSTRAINT `QRTZ_TRIGGERS_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '触发器'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------
INSERT INTO `qrtz_triggers`
VALUES ('quartzScheduler', 'test', 'hello分组', 'test', 'hello分组', 'test', 1746615080000, 1746615075000, 5, 'PAUSED',
        'CRON', 1746615063000, 0, NULL, 0, '');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`
(
    `id`          bigint                                                        NOT NULL COMMENT '唯一id',
    `parent_id`   bigint                                                        NOT NULL DEFAULT 0 COMMENT '父级id',
    `manager`     text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci         NOT NULL COMMENT '管理者id',
    `dept_name`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
    `summary`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门简介',
    `create_time` datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                                                        NOT NULL COMMENT '创建用户',
    `update_user` bigint                                                        NULL     DEFAULT NULL COMMENT '操作用户',
    `is_deleted`  tinyint(1) UNSIGNED ZEROFILL                                  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_dept_name` (`dept_name` ASC) USING BTREE,
    INDEX `idx_summary` (`summary` ASC) USING BTREE,
    INDEX `idx_parent_id` (`parent_id` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间',
    FULLTEXT INDEX `idx_manager` (`manager`)
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '部门表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept`
VALUES (1842844360640327682, 0, '梁子异,彭秀英,戴子韬,曾致远', '山东总公司', '山东总公司', '2024-10-06 16:28:29',
        '2024-11-04 01:44:05', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1842874908557541377, 1842844360640327682, 'bunny,史安琪,邱致远,秦詩涵', '深圳分公司', '深圳分公司',
        '2024-10-06 18:29:52', '2025-04-28 18:30:01', 1, 1849681227633758210, 0);
INSERT INTO `sys_dept`
VALUES (1842877591473455106, 1842844360640327682, '程嘉伦,武嘉伦,孙震南', '郑州分公司', '郑州分公司',
        '2024-10-06 18:40:31', '2024-10-06 19:02:27', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1842883239493881857, 1842877591473455106, '于致远,曹致远,李璐,戴子韬', '研发部门', '研发部门',
        '2024-10-06 19:02:58', '2024-10-06 19:02:58', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1842885275601981441, 1842874908557541377, '宋宇宁,彭秀英,刘子韬', '市场部门', '市场部门', '2024-10-06 19:11:04',
        '2024-10-06 19:11:04', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1842885316358033409, 1842877591473455106, '宋宇宁,曾致远,刘子韬', '财务部门', '财务部门', '2024-10-06 19:11:13',
        '2024-10-09 10:38:35', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1842885375015374850, 1842877591473455106, '冯宇宁,刘子韬,曾致远', '测试部门', '测试部门', '2024-10-06 19:11:27',
        '2024-10-06 19:11:52', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1842885831187877890, 1842877591473455106, '龚岚,杜安琪,李子异,彭睿,宋宇宁,bunny', '运维部门', '运维部门',
        '2024-10-06 19:13:16', '2024-10-06 19:13:16', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1849676669473886209, 1842874908557541377, 'password,Administrator,bunny', '测试部门', '测试部门',
        '2024-10-25 12:57:38', '2024-10-25 12:57:46', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1849676767842897921, 1842874908557541377, 'Administrator', '研发部门', '研发部门', '2024-10-25 12:58:01',
        '2024-10-25 12:58:01', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1850077538510016514, 1842844360640327682, 'Administrator', '江阴分公司', '江阴分公司', '2024-10-26 15:30:33',
        '2024-10-28 10:18:02', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1850077710275153922, 1850077538510016514, 'Administrator,bunny', '运维部门', '运维部门', '2024-10-26 15:31:14',
        '2024-10-26 15:31:14', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1850077827405287425, 1842874908557541377, 'Administrator,bunny', '运维部门', '运维部门', '2024-10-26 15:31:41',
        '2024-10-26 15:31:41', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1850077890558922754, 1850077538510016514, 'Administrator,password,system', '测试部门', '测试部门',
        '2024-10-26 15:31:57', '2024-10-26 15:31:57', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1850078051901214722, 1850077538510016514, 'Administrator,bunny', '系统研发部', '系统研发部',
        '2024-10-26 15:32:35', '2024-10-26 15:32:35', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1850723783395688450, 1842844360640327682, 'Administrator', '淮安分公司', '淮安分公司', '2024-10-28 10:18:29',
        '2024-10-28 10:18:29', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1850723925309964289, 1850723783395688450, 'Administrator', '研发部门', '研发部门', '2024-10-28 10:19:03',
        '2024-10-28 10:19:03', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1850723973024366594, 1850723783395688450, 'Administrator', '测试部门', '测试部门', '2024-10-28 10:19:15',
        '2024-10-28 10:19:15', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1850724041672540161, 1850723783395688450, 'Administrator', '运维部门', '运维部门', '2024-10-28 10:19:31',
        '2024-10-28 10:19:31', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1850724090196443138, 1850723783395688450, 'Administrator', '采购部门', '采购部门', '2024-10-28 10:19:43',
        '2024-10-28 10:19:43', 1, 1, 0);
INSERT INTO `sys_dept`
VALUES (1916802188127846402, 1842885275601981441, 'Administrator,bunny', '生产部门', '生产部门', '2025-04-28 18:30:29',
        '2025-04-28 18:30:29', 1849681227633758210, 1849681227633758210, 0);

ALTER TABLE `sys_dept`
    ADD COLUMN `status` tinyint NULL DEFAULT 0 COMMENT '1:禁用 0:正常' AFTER `summary`;

-- ----------------------------
-- Table structure for sys_email_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_template`;
CREATE TABLE `sys_email_template`
(
    `id`            bigint                                                        NOT NULL COMMENT '唯一id',
    `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
    `email_user`    bigint                                                        NULL     DEFAULT NULL COMMENT '关联邮件用户配置',
    `subject`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主题',
    `body`          text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NOT NULL COMMENT '邮件内容',
    `type`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '邮件类型',
    `is_default`    tinyint(1)                                                    NULL     DEFAULT 0 COMMENT '是否默认',
    `create_time`   datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`   bigint                                                        NOT NULL COMMENT '创建用户',
    `update_user`   bigint                                                        NULL     DEFAULT NULL COMMENT '操作用户',
    `is_deleted`    tinyint(1) UNSIGNED ZEROFILL                                  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_template_name` (`template_name` ASC) USING BTREE COMMENT '模板名称不能重复',
    INDEX `idx_email_user` (`email_user` ASC) USING BTREE,
    INDEX `idx_subject` (`subject` ASC) USING BTREE,
    INDEX `idx_type` (`type` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮件模板表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_email_template
-- ----------------------------
INSERT INTO `sys_email_template`
VALUES (1791870020197625858, 'email-1', 2, '邮箱验证码1s',
        '<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta content=\"IE=edge\" http-equiv=\"X-UA-Compatible\">\n    <meta content=\"width=device-width, initial-scale=1.0\" name=\"viewport\">\n    <title>Email Verification Code</title>\n</head>\n<body style=\"margin: 0; padding: 0; background-color: #f5f5f5;\">\n<div style=\"max-width: 600px; margin: 0 auto;\">\n    <table style=\"width: 100%; border-collapse: collapse; background-color: #ffffff; font-family: Arial, sans-serif;\">\n        <tr>\n            <th style=\"height: 60px; padding: 20px; background-color: #0074d3; border-radius: 5px 5px 0 0;\"\n                valign=\"middle\">\n                <h1 style=\"margin: 0; color: #ffffff; font-size: 24px;\">#title#邮箱验证码</h1>\n            </th>\n        </tr>\n        <tr>\n            <td style=\"padding: 20px;\">\n                <div style=\"background-color: #ffffff; padding: 25px;\">\n                    <h2 style=\"margin: 10px 0; font-size: 18px; color: #333333;\">\n                        尊敬的用户：\n                    </h2>\n                    <p style=\"margin: 10px 0; font-size: 16px; color: #333333;\">\n                        感谢您注册我们的产品. 您的账号正在进行电子邮件验证.\n                    </p>\n                    <p style=\"margin: 10px 0; font-size: 16px; color: #333333;\">\n                        验证码为: <span class=\"button\" style=\"color: #1100ff;\">#verifyCode#</span>\n                    </p>\n                    <p style=\"margin: 10px 0; font-size: 16px; color: #333333;\">\n                        验证码的有效期只有#expires#分钟，请抓紧时间进行验证吧！\n                    </p>\n                    <p style=\"margin: 10px 0; font-size: 16px; color: #dc1818;\">\n                        如果非本人操作,请忽略此邮件\n                    </p>\n                </div>\n            </td>\n        </tr>\n        <tr>\n            <td style=\"text-align: center; padding: 20px; background-color: #f5f5f5;\">\n                <p style=\"margin: 0; font-size: 12px; color: #747474;\">\n                    #title#邮箱验证码<br>\n                    Contact us: #sendEmailUser#\n                </p>\n                <p style=\"margin: 10px 0; font-size: 12px; color: #747474;\">\n                    This is an automated email, please do not reply.<br>\n                    © Company #companyName#\n                </p>\n            </td>\n        </tr>\n    </table>\n</div>\n</body>\n</html>',
        'verification_code', 1, '2024-05-18 16:34:38', '2025-05-06 19:50:56', 0, 1, 0);

-- ----------------------------
-- Table structure for sys_email_users
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_users`;
CREATE TABLE `sys_email_users`
(
    `id`             bigint                                                        NOT NULL,
    `email`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
    `password`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
    `host`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Host地址',
    `port`           int                                                           NOT NULL COMMENT '端口号',
    `smtp_agreement` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL     DEFAULT NULL COMMENT '邮箱协议',
    `open_ssl`       tinyint                                                       NOT NULL DEFAULT 1 COMMENT '是否启用ssl',
    `is_default`     tinyint                                                       NULL     DEFAULT NULL COMMENT '是否为默认邮件',
    `create_time`    timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`    bigint                                                        NOT NULL COMMENT '创建用户',
    `update_user`    bigint                                                        NULL     DEFAULT NULL COMMENT '更新用户',
    `is_deleted`     tinyint(1)                                                    NULL     DEFAULT 0 COMMENT '是否被删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_email` (`email` ASC) USING BTREE COMMENT '邮箱不能重复',
    INDEX `idx_smtp_agreement` (`smtp_agreement` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮箱发送表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_email_users
-- ----------------------------
INSERT INTO `sys_email_users`
VALUES (2, '332411176@qq.com', '111', 'smtp.qq.com', 465, 'smtps', 0, 1, '2024-05-14 18:43:50', '2025-05-09 19:57:40',
        0, 1, 0);

-- ----------------------------
-- Table structure for sys_files
-- ----------------------------
DROP TABLE IF EXISTS `sys_files`;
CREATE TABLE `sys_files`
(
    `id`                bigint                                                        NOT NULL COMMENT '文件id',
    `url`               varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '文件访问地址',
    `size`              bigint                                                        NULL     DEFAULT NULL COMMENT '文件大小，单位字节',
    `file_size_str`     varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '文件大小（字符串）',
    `filename`          varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '文件名称',
    `original_filename` varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '原始文件名',
    `base_path`         varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '基础存储路径',
    `filepath`          varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '存储路径',
    `ext`               varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL     DEFAULT NULL COMMENT '文件扩展名',
    `content_type`      varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT 'MIME类型',
    `platform`          varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL     DEFAULT NULL COMMENT '存储平台',
    `th_url`            varchar(512) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '缩略图访问路径',
    `th_filename`       varchar(256) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '缩略图名称',
    `th_size`           bigint                                                        NULL     DEFAULT NULL COMMENT '缩略图大小，单位字节',
    `th_content_type`   varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '缩略图MIME类型',
    `object_id`         varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL     DEFAULT NULL COMMENT '文件所属对象id',
    `object_type`       varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL     DEFAULT NULL COMMENT '文件所属对象类型，例如用户头像，评价图片',
    `metadata`          text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         NULL COMMENT '文件元数据',
    `user_metadata`     text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         NULL COMMENT '文件用户元数据',
    `th_metadata`       text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         NULL COMMENT '缩略图元数据',
    `th_user_metadata`  text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         NULL COMMENT '缩略图用户元数据',
    `attr`              text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         NULL COMMENT '附加属性',
    `file_acl`          varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL     DEFAULT NULL COMMENT '文件ACL',
    `th_file_acl`       varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL     DEFAULT NULL COMMENT '缩略图文件ACL',
    `hash_info`         text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         NULL COMMENT '哈希信息',
    `upload_id`         varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '上传ID，仅在手动分片上传时使用',
    `upload_status`     int                                                           NULL     DEFAULT NULL COMMENT '上传状态，仅在手动分片上传时使用，1：初始化完成，2：上传完成',
    `download_count`    int                                                           NULL     DEFAULT 0 COMMENT '下载数量',
    `create_user`       bigint                                                        NOT NULL COMMENT '创建用户',
    `update_user`       bigint                                                        NULL     DEFAULT NULL COMMENT '操作用户',
    `create_time`       datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录文件最后修改的时间戳',
    `is_deleted`        tinyint(1) UNSIGNED ZEROFILL                                  NOT NULL DEFAULT 0 COMMENT '文件是否被删除',
    PRIMARY KEY (`id`) USING BTREE COMMENT '主键索引，用于快速定位文件记录',
    INDEX `idx_filename` (`filename` ASC) USING BTREE COMMENT '文件名索引，用于按文件名快速查找文件',
    INDEX `idx_filepath` (`filepath` ASC) USING BTREE COMMENT '文件路径索引，用于按路径快速查找文件',
    INDEX `idx_content_type` (`content_type` ASC) USING BTREE COMMENT '文件类型索引，用于按MIME类型快速查找文件',
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '更新用户索引，用于查找特定用户更新的文件',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '创建用户索引，用于查找特定用户创建的文件',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '联合用户索引，用于同时查询创建和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '时间联合索引，用于按时间范围查询文件',
    INDEX `idx_url` (`url` ASC) USING BTREE COMMENT '文件URL索引，用于快速访问特定URL的文件',
    INDEX `idx_size` (`size` ASC) USING BTREE COMMENT '文件大小索引，用于按文件大小范围查询',
    INDEX `idx_platform` (`platform` ASC) USING BTREE COMMENT '存储平台索引，用于按存储平台查询文件',
    INDEX `idx_ext` (`ext` ASC) USING BTREE COMMENT '文件扩展名索引，用于按文件扩展名查询',
    INDEX `idx_object` (`object_id` ASC, `object_type` ASC) USING BTREE COMMENT '对象联合索引，用于查询特定对象关联的文件',
    INDEX `idx_upload_status` (`upload_status` ASC) USING BTREE COMMENT '上传状态索引，用于查询特定状态的上传文件',
    INDEX `idx_download_count` (`download_count` ASC) USING BTREE COMMENT '下载次数索引，用于查询热门文件',
    INDEX `idx_is_deleted` (`is_deleted` ASC) USING BTREE COMMENT '删除状态索引，用于查询已删除或未删除的文件',
    INDEX `idx_create_time` (`create_time` ASC) USING BTREE COMMENT '创建时间索引，用于按创建时间排序或查询',
    INDEX `idx_update_time` (`update_time` ASC) USING BTREE COMMENT '更新时间索引，用于按最后修改时间排序或查询',
    INDEX `idx_th_content_type` (`th_content_type` ASC) USING BTREE COMMENT '缩略图类型索引，用于按缩略图MIME类型查询',
    INDEX `idx_th_size` (`th_size` ASC) USING BTREE COMMENT '缩略图大小索引，用于按缩略图大小查询'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci COMMENT = '文件记录'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_files
-- ----------------------------

-- ----------------------------
-- Table structure for sys_files_part_detail
-- ----------------------------
DROP TABLE IF EXISTS `sys_files_part_detail`;
CREATE TABLE `sys_files_part_detail`
(
    `id`          varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NOT NULL COMMENT '分片id',
    `platform`    varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL     DEFAULT NULL COMMENT '存储平台',
    `upload_id`   varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '上传ID，仅在手动分片上传时使用',
    `e_tag`       varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL     DEFAULT NULL COMMENT '分片 ETag',
    `part_number` int                                                           NULL     DEFAULT NULL COMMENT '分片号。每一个上传的分片都有一个分片号，一般情况下取值范围是1~10000',
    `part_size`   bigint                                                        NULL     DEFAULT NULL COMMENT '文件大小，单位字节',
    `hash_info`   text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         NULL COMMENT '哈希信息',
    `create_user` bigint                                                        NOT NULL COMMENT '创建用户',
    `update_user` bigint                                                        NULL     DEFAULT NULL COMMENT '操作用户',
    `create_time` datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录文件最后修改的时间戳',
    `is_deleted`  tinyint(1) UNSIGNED ZEROFILL                                  NOT NULL DEFAULT 0 COMMENT '文件是否被删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci COMMENT = '文件分片信息表，仅在手动分片上传时使用'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_files_part_detail
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu_icon
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu_icon`;
CREATE TABLE `sys_menu_icon`
(
    `id`          bigint                                                        NOT NULL,
    `icon_code`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'icon类名',
    `icon_name`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'icon 名称',
    `create_time` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime                                                      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                                                        NOT NULL COMMENT '创建用户',
    `update_user` bigint                                                        NULL DEFAULT NULL COMMENT '操作用户',
    `is_deleted`  tinyint                                                       NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `icon_code` (`icon_code` ASC) USING BTREE COMMENT '图标code不能重复',
    INDEX `idx_icon_name` (`icon_name` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '图标code不能重复'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu_icon
-- ----------------------------
INSERT INTO `sys_menu_icon`
VALUES (1837123533672370177, 'material-symbols:view-carousel', '轮播图', '2024-09-20 21:35:57', '2024-10-05 19:23:27',
        1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837127664847831041, 'line-md:iconify1', '消息', '2024-09-20 21:52:22', '2024-10-05 20:51:08', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837128141505314817, 'line-md:email-filled', '邮箱', '2024-09-20 21:54:16', '2024-10-05 20:58:06', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837128346233487361, 'material-symbols:router', '消息', '2024-09-20 21:55:05', '2024-10-05 20:57:56', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837128498058903553, 'octicon:log-16', '日志', '2024-09-20 21:55:41', '2024-10-05 20:51:57', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837128688530636802, 'ion:navigate', '导航', '2024-09-20 21:56:26', '2024-10-05 19:24:20', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837128993204879361, 'grommet-icons:user-manager', '用户', '2024-09-20 21:57:39', '2024-10-05 19:24:13', 1, 1,
        0);
INSERT INTO `sys_menu_icon`
VALUES (1837129632592969729, 'ic:round-manage-accounts', '用户管理', '2024-09-20 22:00:11', '2024-10-05 19:24:07', 1, 1,
        0);
INSERT INTO `sys_menu_icon`
VALUES (1837129891234725890, 'fluent-mdl2:manager-self-service', '权限', '2024-09-20 22:01:13', '2024-10-05 19:24:01',
        1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837130085451972609, 'carbon:user-role', '权限', '2024-09-20 22:01:59', '2024-10-05 19:23:55', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837130248237105154, 'oui:app-users-roles', '角色管理', '2024-09-20 22:02:38', '2024-10-05 19:23:47', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837132226136653826, 'material-symbols:language', '国际化', '2024-09-20 22:10:30', '2024-10-05 19:23:38', 1, 1,
        0);
INSERT INTO `sys_menu_icon`
VALUES (1837132285360226305, 'clarity:language-line', '国际化', '2024-09-20 22:10:44', '2024-10-05 19:23:41', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837132323981377537, 'clarity:language-solid', '国际化', '2024-09-20 22:10:53', '2024-10-05 21:17:06', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1837132606656495617, 'carbon:router', '路由', '2024-09-20 22:12:00', '2024-10-05 19:23:20', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1841516265060802562, 'ion:albums-outline', '轮播图', '2024-10-03 00:31:06', '2024-10-05 20:51:50', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1843935459918163970, 'line-md:file-filled', '文件管理', '2024-10-09 16:44:07', '2024-10-09 16:44:07', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844291727040147458, 'fluent-mdl2:chart-template', '模板', '2024-10-10 16:19:48', '2024-10-28 10:09:44', 1, 1,
        0);
INSERT INTO `sys_menu_icon`
VALUES (1844660350732599297, 'carbon:cloud-monitoring', '系统监控', '2024-10-11 16:44:34', '2024-10-11 16:44:34', 1, 1,
        0);
INSERT INTO `sys_menu_icon`
VALUES (1844660527954526209, 'bxs:server', '服务', '2024-10-11 16:45:17', '2024-10-11 16:45:17', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844903931720433665, 'hugeicons:configuration-01', '系统配置', '2024-10-12 08:52:29', '2024-10-12 08:52:29', 1,
        1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844904858841972737, 'subway:menu', '菜单', '2024-10-12 08:56:10', '2024-10-12 08:56:10', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844905064786493441, 'tdesign:file-icon', '图标', '2024-10-12 08:56:59', '2024-10-12 08:56:59', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844931953475416065, 'raphael:db', 'db', '2024-10-12 10:43:50', '2024-10-12 10:43:50', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844932695623954433, 'devicon:redis', 'redis', '2024-10-12 10:46:46', '2024-10-12 10:46:46', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844933323939082242, 'material-symbols:terminal', 'terminal', '2024-10-12 10:49:16', '2024-10-12 10:49:16', 1,
        1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844933588884877314, 'simple-icons:minio', 'minio', '2024-10-12 10:50:19', '2024-10-12 10:50:19', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844933885178900482, 'mage:compact-disk', 'disk', '2024-10-12 10:51:30', '2024-10-12 10:51:30', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844935187489320962, 'devicon:swagger', 'swagger', '2024-10-12 10:56:41', '2024-10-12 10:56:41', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844935519208435714, 'file-icons:swagger', 'swagger', '2024-10-12 10:58:00', '2024-10-12 10:58:00', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844954519585890305, 'logos:element', '饿了么', '2024-10-12 12:13:30', '2024-10-12 12:13:30', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844954711328497665, 'file-icons:pure', 'pureadmin', '2024-10-12 12:14:15', '2024-10-12 12:14:15', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844955395549503489, 'mdi:iframe-outline', 'iframe', '2024-10-12 12:16:59', '2024-10-12 12:16:59', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844963007485640706, 'noto:letter-k', 'k', '2024-10-12 12:47:13', '2024-10-12 12:47:13', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844963373547716609, 'mdi:iframe-braces', 'iframe', '2024-10-12 12:48:41', '2024-10-12 12:48:41', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1844966804500971522, 'line-md:link', '链接', '2024-10-12 13:02:19', '2024-10-12 13:02:19', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1845817076322480129, 'simple-icons:apachedolphinscheduler', '任务调度', '2024-10-14 21:20:59',
        '2024-10-14 21:20:59', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1846832706716651521, 'uis:layer-group', '分组', '2024-10-17 16:36:44', '2024-10-17 16:36:44', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1846832884685164546, 'mingcute:time-fill', '时间', '2024-10-17 16:37:27', '2024-10-17 16:37:27', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1847144431877865474, 'icon-park-solid:log', '日志', '2024-10-18 13:15:26', '2024-10-18 13:15:26', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1847144521539502081, 'ph:log-bold', '日志', '2024-10-18 13:15:47', '2024-10-18 13:15:47', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1848233211644801026, 'ph:clock-user', '用户', '2024-10-21 13:21:51', '2024-10-21 13:21:51', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1848233345497624578, 'eos-icons:cronjob', '任务', '2024-10-21 13:22:23', '2024-10-21 13:22:23', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1848991874416324610, 'octicon:cache-16', '缓存', '2024-10-23 15:36:30', '2024-10-23 15:36:30', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1849278944816918529, 'vscode-icons:file-type-jsconfig', '配置', '2024-10-24 10:37:13', '2024-10-24 10:37:13', 1,
        1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1849677540270116865, 'mingcute:web-fill', 'web', '2024-10-25 13:01:06', '2024-10-25 13:01:06', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1849677639935168514, 'mingcute:server-fill', 'server', '2024-10-25 13:01:29', '2024-10-25 13:01:29', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1851511239723253761, 'lets-icons:message-alt-fill', '消息', '2024-10-30 14:27:34', '2024-10-30 15:26:57',
        1849444494908125181, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1851511559568293890, 'hugeicons:message-edit-02', '消息管理', '2024-10-30 14:28:50', '2024-10-30 15:25:41',
        1849444494908125181, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1851525682550214657, 'tabler:message-circle-cog', '消息', '2024-10-30 15:24:57', '2024-10-30 15:24:57', 1, 1,
        0);
INSERT INTO `sys_menu_icon`
VALUES (1851527033812357121, 'wpf:gps-receiving', '接收消息', '2024-10-30 15:30:19', '2024-10-30 15:30:19', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1851527456963104769, 'grommet-icons:vm-maintenance', '系统维护', '2024-10-30 15:32:00', '2024-10-30 15:32:00',
        1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1873365450202648578, 'file-icons:config-react', '系统配置', '2024-12-29 21:48:23', '2024-12-29 21:48:23', 1, 1,
        0);
INSERT INTO `sys_menu_icon`
VALUES (1915426199414087681, 'entypo:email', '邮箱', '2025-04-24 23:22:47', '2025-04-24 23:22:47', 1, 1, 0);
INSERT INTO `sys_menu_icon`
VALUES (1918949253637214209, 'material-symbols:login', '登录', '2025-05-04 16:42:09', '2025-05-04 16:42:09', 1, 1, 0);

-- ----------------------------
-- Table structure for sys_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message`
(
    `id`           bigint                                                        NOT NULL COMMENT 'ID',
    `title`        varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息标题',
    `send_user_id` bigint                                                        NULL DEFAULT NULL COMMENT '发送人用户ID',
    `message_type` bigint                                                        NULL DEFAULT NULL COMMENT 'sys:系统消息,user用户消息',
    `cover`        varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面',
    `summary`      varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简介',
    `content`      text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '消息内容',
    `editor_type`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '编辑器类型',
    `level`        varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '消息等级',
    `extra`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息等级详情',
    `create_time`  datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user`  bigint                                                        NULL DEFAULT NULL COMMENT '操作用户',
    `create_user`  bigint                                                        NULL DEFAULT NULL COMMENT '创建用户',
    `is_deleted`   tinyint                                                       NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_message_type` (`message_type` ASC) USING BTREE,
    INDEX `idx_editor_type` (`editor_type` ASC) USING BTREE,
    INDEX `idx_send_user_id` (`send_user_id` ASC) USING BTREE,
    INDEX `idx_level` (`level` ASC) USING BTREE,
    INDEX `idx_extra` (`extra` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间',
    CONSTRAINT `sys_message_ibfk_1` FOREIGN KEY (`message_type`) REFERENCES `sys_message_type` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统消息'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_message
-- ----------------------------
INSERT INTO `sys_message`
VALUES (1920759649130053634, 'sss', 1, 1851498066518687745,
        '/api/local-file/message/2025-05-09/681dbe55cdfb33f3a83edaed.jpg', 'sss',
        'c3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3Nzc3NzIVtdKC9hcGkvbG9jYWwtZmlsZS9tZXNzYWdlLzIwMjUtMDUtMDkvNjgxZGJlNjJjZGZiMzNmM2E4M2VkYWVlLnBuZykK',
        'markdown', 'info', 'sss', '2025-05-09 16:36:01', '2025-05-09 16:36:01', 1, 1, 0);
INSERT INTO `sys_message`
VALUES (1920762372042866689, 'thUrl', 1, 1851498066518687745,
        '/api/local-file/message/2025-05-09/681dc1eecdfbce75ed403bd2.jpg.min.jpg', 'thUrl',
        'IVtdKC9hcGkvbG9jYWwtZmlsZS9tZXNzYWdlLzIwMjUtMDUtMDkvNjgxZGMwZWVjZGZiY2U3NWVkNDAzYmNmLnBuZy5taW4uanBnKQohW10oL2FwaS9sb2NhbC1maWxlL21lc3NhZ2UvMjAyNS0wNS0wOS82ODFkYzIyMmNkZmJjZTc1ZWQ0MDNiZDMucG5nLm1pbi5qcGcpCiFbXSgvYXBpL2xvY2FsLWZpbGUvbWVzc2FnZS8yMDI1LTA1LTA5LzY4MWRjMjQyY2RmYmNlNzVlZDQwM2JkNC5wbmcpCg==',
        'markdown', 'danger', 'thUr', '2025-05-09 16:46:50', '2025-05-09 16:52:23', 1, 1, 0);
INSERT INTO `sys_message`
VALUES (1920763084927746049, '杀杀杀', 1, 1851498066518687745,
        '/api/local-file/message/2025-05-09/681dc19acdfbce75ed403bd1.png.min.jpg', '杀杀杀',
        'IVtdKC9hcGkvbG9jYWwtZmlsZS9tZXNzYWdlLzIwMjUtMDUtMDkvNjgxZGMxOTVjZGZiY2U3NWVkNDAzYmQwLnBuZykK', 'markdown',
        'info', '杀杀杀', '2025-05-09 16:49:40', '2025-05-09 16:49:40', 1, 1, 0);

-- ----------------------------
-- Table structure for sys_message_received
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_received`;
CREATE TABLE `sys_message_received`
(
    `id`               bigint   NOT NULL COMMENT '主键',
    `received_user_id` bigint   NULL DEFAULT NULL COMMENT '接受者用户ID',
    `message_id`       bigint   NULL DEFAULT NULL COMMENT '消息ID',
    `status`           tinyint  NULL DEFAULT 0 COMMENT '0:未读 1:已读',
    `create_time`      datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_user`      bigint   NULL DEFAULT NULL COMMENT '操作用户',
    `create_user`      bigint   NULL DEFAULT NULL COMMENT '创建用户',
    `is_deleted`       tinyint  NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_received_user_id` (`received_user_id` ASC) USING BTREE,
    INDEX `idx_message_id` (`message_id` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间',
    CONSTRAINT `sys_message_received_ibfk_1` FOREIGN KEY (`message_id`) REFERENCES `sys_message` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_message_received
-- ----------------------------
INSERT INTO `sys_message_received`
VALUES (1920759649146830849, 1, 1920759649130053634, 1, '2025-05-09 16:36:01', '2025-05-09 16:52:49', 1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920759649146830850, 1849444494908125181, 1920759649130053634, 0, '2025-05-09 16:36:01', '2025-05-09 16:52:36',
        1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920759649146830851, 1849681227633758210, 1920759649130053634, 0, '2025-05-09 16:36:01', '2025-05-09 16:52:36',
        1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920759649151025154, 1850080272764211202, 1920759649130053634, 0, '2025-05-09 16:36:01', '2025-05-09 16:52:36',
        1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920763084927746050, 1, 1920763084927746049, 1, '2025-05-09 16:49:40', '2025-05-09 16:52:47', 1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920763084927746051, 1849444494908125181, 1920763084927746049, 0, '2025-05-09 16:49:40', '2025-05-09 16:52:36',
        1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920763084927746052, 1849681227633758210, 1920763084927746049, 0, '2025-05-09 16:49:40', '2025-05-09 16:52:36',
        1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920763084927746053, 1850080272764211202, 1920763084927746049, 0, '2025-05-09 16:49:40', '2025-05-09 16:52:36',
        1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920763769085837313, 1, 1920762372042866689, 1, '2025-05-09 16:52:23', '2025-05-09 16:52:42', 1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920763769085837314, 1849444494908125181, 1920762372042866689, 0, '2025-05-09 16:52:23', '2025-05-09 16:52:36',
        1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920763769085837315, 1849681227633758210, 1920762372042866689, 0, '2025-05-09 16:52:23', '2025-05-09 16:52:36',
        1, 1, 0);
INSERT INTO `sys_message_received`
VALUES (1920763769085837316, 1850080272764211202, 1920762372042866689, 0, '2025-05-09 16:52:23', '2025-05-09 16:52:36',
        1, 1, 0);

-- ----------------------------
-- Table structure for sys_message_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_type`;
CREATE TABLE `sys_message_type`
(
    `id`           bigint                                                         NOT NULL COMMENT 'ID',
    `message_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT NULL COMMENT '消息名称',
    `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT NULL COMMENT '消息类型',
    `summary`      varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息备注',
    `status`       tinyint                                                        NULL DEFAULT NULL COMMENT '0:启用 1:禁用',
    `create_time`  datetime                                                       NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  timestamp                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_user`  bigint                                                         NULL DEFAULT NULL COMMENT '创建用户',
    `update_user`  bigint                                                         NULL DEFAULT NULL COMMENT '操作用户',
    `is_deleted`   tinyint(1) UNSIGNED ZEROFILL                                   NULL DEFAULT 0 COMMENT '是否被删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_message_type` (`message_type` ASC) USING BTREE,
    INDEX `idx_message_name` (`message_name` ASC) USING BTREE,
    INDEX `idx_status` (`status` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统消息类型'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_message_type
-- ----------------------------
INSERT INTO `sys_message_type`
VALUES (1851498066518687745, '用户消息类型', 'user', '用户消息类型啊', 1, '2024-10-30 13:35:13', '2025-04-26 22:10:21',
        1, 1, 0);
INSERT INTO `sys_message_type`
VALUES (1851507850609356802, '系统消息类型', 'sys', '系统消息类型', 1, '2024-10-30 14:14:06', '2025-04-26 22:10:56', 1,
        1, 0);
INSERT INTO `sys_message_type`
VALUES (1851894134725136386, '通知消息', 'notifications', '通知消息，用户用户接受通知消息', 1, '2024-10-31 15:49:03',
        '2024-10-31 15:49:03', 1, 1, 0);

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`
(
    `id`             bigint                                                        NOT NULL COMMENT '权限ID',
    `parent_id`      bigint                                                        NULL DEFAULT NULL COMMENT '父级id',
    `power_code`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码',
    `power_name`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
    `request_url`    text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci         NULL COMMENT '请求路径',
    `request_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法',
    `create_time`    datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime                                                      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user`    bigint                                                        NULL DEFAULT NULL COMMENT '创建用户',
    `update_user`    bigint                                                        NULL DEFAULT NULL COMMENT '更新用户',
    `is_deleted`     tinyint(1) UNSIGNED ZEROFILL                                  NULL DEFAULT 0 COMMENT '是否删除，0-未删除，1-已删除',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_parent_id` (`parent_id` ASC) USING BTREE,
    INDEX `idx_power_name` (`power_name` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间',
    INDEX `power_code` (`power_code` ASC, `power_name` ASC, `request_method` ASC) USING BTREE,
    FULLTEXT INDEX `idx_request_url` (`request_url`),
    FULLTEXT INDEX `idx_request_method` (`request_method`) COMMENT '索引请求方法'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统权限表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission`
VALUES (1920809740335415298, 0, 'menuIcon:*', '菜单图标', '/api/menuIcon', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740335415299, 1920809740335415298, 'menuIcon:query', '分页查询菜单图标', '/api/menuIcon/*/*', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740335415300, 1920809740335415298, 'menuIcon:add', '添加菜单图标', '/api/menuIcon', 'POST',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740335415301, 1920809740335415298, 'menuIcon:update', '更新菜单图标', '/api/menuIcon', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740335415302, 1920809740335415298, 'menuIcon:delete', '删除菜单图标', '/api/menuIcon', 'DELETE',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740398329858, 0, 'schedulers:*', '任务调度', '/api/schedulers', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740398329859, 1920809740398329858, 'schedulers:update', '恢复任务调度', '/api/schedulers/resume', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740398329860, 1920809740398329858, 'schedulers:delete', '删除任务调度', '/api/schedulers', 'DELETE',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740398329861, 1920809740398329858, 'schedulers:add', '添加任务调度', '/api/schedulers', 'POST',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740398329862, 1920809740398329858, 'schedulers:update', '更新任务调度', '/api/schedulers', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740398329863, 1920809740398329858, 'schedulers:query', '分页查询任务调度', '/api/schedulers/*/*', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740398329864, 1920809740398329858, 'schedulers:update', '暂停任务调度', '/api/schedulers/pause', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740469633025, 0, 'rolePermission:*', '角色和权限', '/api/rolePermission', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740469633026, 1920809740469633025, 'rolePermission:update', '为角色分配权限', '/api/rolePermission',
        'POST', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740469633027, 0, 'messageType:*', '消息类型', '/api/messageType', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740532547586, 1920809740469633027, 'messageType:query', '分页查询消息类型', '/api/messageType/*/*',
        'GET', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740532547587, 1920809740469633027, 'messageType:add', '添加消息类型', '/api/messageType', 'POST',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740532547588, 1920809740469633027, 'messageType:update', '更新消息类型', '/api/messageType', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740532547589, 1920809740469633027, 'messageType:delete', '删除消息类型', '/api/messageType', 'DELETE',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740595462145, 0, 'userLoginLog:*', '用户登录日志', '/api/userLoginLog', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740595462146, 1920809740595462145, 'userLoginLog:delete', '删除用户登录日志', '/api/userLoginLog',
        'DELETE', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740595462147, 1920809740595462145, 'userLoginLog:query', '分页查询用户登录日志', '/api/userLoginLog/*/*',
        'GET', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740595462148, 0, '', '普通用户登录', '/api/user', NULL, '2025-05-09 19:55:04', '2025-05-09 19:55:04',
        NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740662571010, 0, 'emailUsers:*', '邮箱用户配置', '/api/emailUsers', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740662571011, 1920809740662571010, 'emailUsers:query', '分页查询邮箱用户配置', '/api/emailUsers/*/*',
        'GET', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740662571012, 1920809740662571010, 'emailUsers:delete', '删除邮箱用户配置', '/api/emailUsers', 'DELETE',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740662571013, 1920809740662571010, 'emailUsers:add', '添加邮箱用户配置', '/api/emailUsers', 'POST',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740662571014, 1920809740662571010, 'emailUsers:update', '更新邮箱用户配置', '/api/emailUsers', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740729679881, 0, 'userRole:*', '用户和角色', '/api/userRole', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740792594434, 1920809740729679881, 'userRole:add', '为用户分配角色', '/api/userRole', 'POST',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740792594435, 0, 'files:*', '文件', '/api/files', NULL, '2025-05-09 19:55:04', '2025-05-09 19:55:04',
        NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740792594436, 1920809740792594435, 'files:update', '更新文件', '/api/files', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740792594437, 1920809740792594435, 'files:query', '分页查询文件', '/api/files/*/*', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740792594438, 1920809740792594435, 'files:delete', '删除文件', '/api/files', 'DELETE',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740792594439, 1920809740792594435, 'files:query', '下载文件', '/api/files/file/*', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740792594440, 1920809740792594435, 'files:add', '添加文件', '/api/files', 'POST', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740855508994, 0, 'scheduleExecuteLog:*', '任务调度执行日志', '/api/scheduleExecuteLog', NULL,
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740855508995, 1920809740855508994, 'scheduleExecuteLog:query', '分页查询任务调度执行日志',
        '/api/scheduleExecuteLog/*/*', 'GET', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740855508996, 1920809740855508994, 'scheduleExecuteLog:delete', '删除任务调度执行日志',
        '/api/scheduleExecuteLog', 'DELETE', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740922617857, 0, 'filesParDetail:*', '文件分片信息表，仅在手动分片上传时使用', '/api/filesParDetail',
        NULL, '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740922617858, 1920809740922617857, 'filesParDetail:add', '添加文件分片信息表，仅在手动分片上传时使用',
        '/api/filesParDetail', 'POST', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740922617859, 1920809740922617857, 'filesParDetail:delete', '删除文件分片信息表，仅在手动分片上传时使用',
        '/api/filesParDetail', 'DELETE', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740922617860, 1920809740922617857, 'filesParDetail:update', '更新文件分片信息表，仅在手动分片上传时使用',
        '/api/filesParDetail', 'PUT', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740922617861, 1920809740922617857, 'filesParDetail:query',
        '分页查询文件分片信息表，仅在手动分片上传时使用', '/api/filesParDetail/*/*', 'GET', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740922617862, 0, 'message:*', '系统消息', '/api/message', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740989726722, 1920809740922617862, '', '更系统消息', '/api/message', 'PUT', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740989726723, 1920809740922617862, '', '删除系统消息', '/api/message', 'DELETE', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740989726724, 1920809740922617862, 'message:query', '分页查询系统消息', '/api/message/*/*', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740989726725, 1920809740922617862, '', '添加系统消息', '/api/message', 'POST', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740989726726, 0, 'messageReceived:*', '消息接收(用户消息)', '/api/messageReceived', NULL,
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740989726727, 1920809740989726726, 'messageReceived:delete', '删除消息接收', '/api/messageReceived',
        'DELETE', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740989726728, 1920809740989726726, 'messageReceived:update', '更新消息接收', '/api/messageReceived',
        'PUT', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809740989726729, 1920809740989726726, 'messageReceived:query', '分页查询消息接收',
        '/api/messageReceived/*/*', 'GET', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741052641282, 0, 'routerRole:*', '路由菜单和角色', '/api/routerRole', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741052641283, 1920809741052641282, 'routerRole:delete', '清除选中菜单所有角色',
        '/api/routerRole/clearRouterRole', 'DELETE', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741052641284, 0, 'schedulersGroup:*', '任务调度分组', '/api/schedulersGroup', NULL,
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741119750145, 1920809741052641284, 'schedulersGroup:add', '添加任务调度分组', '/api/schedulersGroup',
        'POST', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741119750146, 1920809741052641284, 'schedulersGroup:query', '获取所有任务调度分组',
        '/api/schedulersGroup/getSchedulersGroupList', 'GET', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL,
        0);
INSERT INTO `sys_permission`
VALUES (1920809741119750147, 1920809741052641284, 'schedulersGroup:update', '更新任务调度分组', '/api/schedulersGroup',
        'PUT', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741119750148, 1920809741052641284, 'schedulersGroup:query', '分页查询任务调度分组',
        '/api/schedulersGroup/*/*', 'GET', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741119750149, 1920809741052641284, 'schedulersGroup:delete', '删除任务调度分组', '/api/schedulersGroup',
        'DELETE', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741119750150, 0, 'dept:*', '部门', '/api/dept', NULL, '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL,
        NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664705, 1920809741119750150, 'dept:query', '分页查询部门', '/api/dept/*/*', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664706, 1920809741119750150, 'dept:delete', '删除部门', '/api/dept', 'DELETE',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664707, 1920809741119750150, 'dept:update', '更新部门', '/api/dept', 'PUT', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664708, 1920809741119750150, 'dept:add', '添加部门', '/api/dept', 'POST', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664709, 0, 'role:*', '角色', '/api/role', NULL, '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL,
        NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664710, 1920809741182664709, 'role:update', '更新角色', '/api/role', 'PUT', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664711, 1920809741182664709, 'role:delete', '删除角色', '/api/role', 'DELETE',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664712, 1920809741182664709, 'role:add', '添加角色', '/api/role', 'POST', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664713, 1920809741182664709, 'role:query', '分页查询角色', '/api/role/*/*', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664714, 1920809741182664709, 'role:update', '更新角色列表', '/api/role/file/import', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741182664715, 1920809741182664709, 'role:update', '导出角色列表', '/api/role/file/export', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741245579265, 0, 'permission::all', '权限', '/api/permission', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741245579266, 1920809741245579265, 'permission::update', '更新权限', '/api/permission', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741245579267, 1920809741245579265, 'permission::update', '导出权限', '/api/permission/file/export',
        'GET', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741245579268, 1920809741245579265, 'permission::delete', '删除权限', '/api/permission', 'DELETE',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741245579269, 1920809741245579265, 'permission::add', '添加权限', '/api/permission', 'POST',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741245579270, 1920809741245579265, 'permission::update', '导入权限', '/api/permission/file/import',
        'PUT', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741245579271, 1920809741245579265, 'permission::page', '分页查询权限', '/api/permission/*/*', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741245579272, 1920809741245579265, 'permission::update', '批量修改权',
        '/api/permission/update/permissionBatch', 'PATCH', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741245579273, 1920809741245579265, 'permission::update', '批量修改权限父级',
        '/api/permission/update/permissionListByParentId', 'PATCH', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL,
        NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741312688130, 0, 'router:*', '路由菜单', '/api/router', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741312688131, 1920809741312688130, 'router:query', '查询管理路由菜单', '/api/router/routerList', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741312688132, 1920809741312688130, 'router:add', '添加路由菜单', '/api/router', 'POST',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741312688133, 1920809741312688130, 'router:delete', '删除路由菜单', '/api/router', 'DELETE',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741312688134, 1920809741312688130, 'router:update', '更新路由菜单', '/api/router', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741383991297, 0, 'user:*', '用户', '/api/user', NULL, '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL,
        NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741383991298, 1920809741383991297, 'user:delete', '删除用户', '/api/user', 'DELETE',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741383991299, 1920809741383991297, 'user:query', '分页查询用户', '/api/user/*/*', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741383991300, 1920809741383991297, 'user:add', '添加用户', '/api/user', 'POST', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741383991301, 1920809741383991297, 'user:query', '已登录用户', '/api/user/getCacheUserPage/*/*', 'GET',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741383991302, 1920809741383991297, 'user:update', '更新用户', '/api/user', 'PUT', '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741383991303, 1920809741383991297, 'user:update', '强制退出用户', '/api/user/forcedOffline', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741451100161, 0, 'config:*', 'web配置文件', '/api/config', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741451100162, 1920809741451100161, 'config::update', '更新web配置文件', '/api/config', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741451100163, 0, 'emailTemplate:*', '邮件模板', '/api/emailTemplate', NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741451100164, 1920809741451100163, 'emailTemplate:add', '添加邮件模板', '/api/emailTemplate', 'POST',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741451100165, 1920809741451100163, 'emailTemplate:delete', '删除邮件模板', '/api/emailTemplate',
        'DELETE', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741451100166, 1920809741451100163, 'emailTemplate:query', '分页查询邮件模板', '/api/emailTemplate/*/*',
        'GET', '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741514014722, 1920809741451100163, 'emailTemplate:update', '更新邮件模板', '/api/emailTemplate', 'PUT',
        '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741514014723, 0, 'admin:actuator', 'actuator端点访问', NULL, NULL, '2025-05-09 19:55:04',
        '2025-05-09 19:55:04', NULL, NULL, 0);
INSERT INTO `sys_permission`
VALUES (1920809741514014724, 1920809741514014723, 'actuator:all', 'Springboot端点全部可以访问', '/api/actuator/**',
        NULL, '2025-05-09 19:55:04', '2025-05-09 19:55:04', NULL, NULL, 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `id`          bigint                                                        NOT NULL,
    `role_code`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '角色代码',
    `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL     DEFAULT NULL COMMENT '描述',
    `create_time` datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                                                      NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                                                        NOT NULL COMMENT '创建用户',
    `update_user` bigint                                                        NULL     DEFAULT NULL COMMENT '操作用户',
    `is_deleted`  tinyint(1) UNSIGNED ZEROFILL                                  NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `role_code` (`role_code` ASC) USING BTREE COMMENT '角色码不能重复',
    INDEX `idx_description` (`description` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role`
VALUES (1, 'admin', '管理员用户', '2025-04-28 17:38:59', '2025-04-29 23:06:13', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1852621694771773442, 'actuator', 'actuator端点可访问', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1915784833780183043, 'page::system::dept', '系统部门', '2025-04-28 17:38:59', '2025-04-29 22:57:54', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1915784833780183044, 'page::system::files', '系统文件', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1915784833780183045, 'page::system::power', '系统权限', '2025-04-28 17:38:59', '2025-05-02 20:35:30', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1915784833780183046, 'page::system::role', '系统角色', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1915784833796960257, 'page::system::rolePower', '系统角色和权限', '2025-04-28 17:38:59', '2025-04-28 17:38:59',
        1, 1, 0);
INSERT INTO `sys_role`
VALUES (1915784833796960258, 'page::system::router', '系统路由', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1915784833796960259, 'page::system::routerRole', '系统路由和角色', '2025-04-28 17:38:59', '2025-04-28 17:38:59',
        1, 1, 0);
INSERT INTO `sys_role`
VALUES (1915786941359206409, 'page::system::user', '系统用户管理', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1, 1,
        0);
INSERT INTO `sys_role`
VALUES (1915786941359206410, 'page::system::userRole', '系统用户和角色', '2025-04-28 17:38:59', '2025-04-28 17:38:59',
        1, 1, 0);
INSERT INTO `sys_role`
VALUES (1916789229422989313, 'page::extend', '外部页面', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903873, 'page::config::menuIcon', '菜单图标', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1, 1,
        0);
INSERT INTO `sys_role`
VALUES (1916789229485903874, 'page::config::email-user', '邮件用户配置', '2025-04-28 17:38:59', '2025-04-28 17:38:59',
        1, 1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903875, 'page::config::web-configuration', 'web配置', '2025-04-28 17:38:59', '2025-04-28 17:38:59',
        1, 1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903876, 'page::config::email-template', '邮件模板', '2025-04-28 17:38:59', '2025-04-28 17:38:59',
        1, 1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903877, 'page::monitor::server', '服务监控', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1, 1,
        0);
INSERT INTO `sys_role`
VALUES (1916789229485903878, 'page::monitor::caches', '系统缓存', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1, 1,
        0);
INSERT INTO `sys_role`
VALUES (1916789229485903879, 'page::scheduler::schedulers', '调度任务', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1,
        1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903880, 'page::scheduler::schedulers-group', '任务调度分组', '2025-04-28 17:38:59',
        '2025-04-28 17:38:59', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903883, 'page::logManagement::user-login-log', '任务执行日志', '2025-04-28 17:38:59',
        '2025-04-28 17:38:59', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903884, 'page::logManagement::scheduler-execute-log', '用户登录日志', '2025-04-28 17:38:59',
        '2025-04-28 17:38:59', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903885, 'page::message::message-type', '消息类型', '2025-04-28 17:38:59', '2025-04-28 17:38:59', 1,
        1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903886, 'page::message::message-send', '消息发送管理', '2025-04-28 17:38:59',
        '2025-04-28 17:38:59', 1, 1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903887, 'page::message::message-editing', '消息编辑', '2025-04-28 17:38:59', '2025-04-28 17:38:59',
        1, 1, 0);
INSERT INTO `sys_role`
VALUES (1916789229485903888, 'page::message::message-received', '消息接收管理', '2025-04-28 17:38:59',
        '2025-04-28 17:38:59', 1, 1, 0);

ALTER TABLE `sys_role`
    ADD COLUMN `status` tinyint NULL DEFAULT 0 COMMENT '1:禁用 0:正常' AFTER `description`;

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`
(
    `id`          bigint                       NOT NULL COMMENT 'ID',
    `role_id`     bigint                       NOT NULL COMMENT '角色id',
    `power_id`    bigint                       NOT NULL COMMENT '权限id',
    `create_time` datetime                     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                       NULL DEFAULT NULL COMMENT '创建用户',
    `update_user` bigint                       NULL DEFAULT NULL COMMENT '更新用户',
    `is_deleted`  tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '是否删除，0-未删除，1-已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_role_power` (`role_id` ASC, `power_id` ASC) USING BTREE COMMENT '角色和权限两种不能重复',
    INDEX `idx_role_id` (`role_id` ASC) USING BTREE,
    INDEX `idx_power_id` (`power_id` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间',
    CONSTRAINT `sys_role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `sys_role_permission_ibfk_2` FOREIGN KEY (`power_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色权限表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------

-- ----------------------------
-- Table structure for sys_router
-- ----------------------------
DROP TABLE IF EXISTS `sys_router`;
CREATE TABLE `sys_router`
(
    `id`          bigint                                                        NOT NULL COMMENT '主键id',
    `parent_id`   bigint                                                        NULL     DEFAULT NULL COMMENT '父级id',
    `path`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '在项目中路径',
    `route_name`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '路由名称',
    `component`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '组件位置',
    `redirect`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '路由重定向',
    `menu_type`   int                                                           NULL     DEFAULT NULL COMMENT '菜单类型',
    `meta`        varchar(700) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL     DEFAULT NULL COMMENT '路由meta',
    `create_user` bigint                                                        NULL     DEFAULT NULL COMMENT '创建用户',
    `update_user` bigint                                                        NULL     DEFAULT NULL COMMENT '操作用户',
    `update_time` timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录文件最后修改的时间戳',
    `create_time` timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted`  tinyint(1) UNSIGNED ZEROFILL                                  NOT NULL DEFAULT 0 COMMENT '文件是否被删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_path` (`path` ASC) USING BTREE COMMENT '唯一路由路径',
    UNIQUE INDEX `unique_router_name` (`route_name` ASC) USING BTREE COMMENT '唯一路由名称',
    INDEX `idx_id_parent_id` (`id` ASC, `parent_id` ASC) USING BTREE COMMENT 'id和父级id索引',
    INDEX `idx_id` (`id` ASC) USING BTREE COMMENT 'id',
    INDEX `idx_parent_id` (`parent_id` ASC) USING BTREE COMMENT '父级id索引',
    INDEX `idx_component` (`component` ASC) USING BTREE COMMENT '组件索引',
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '系统菜单表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_router
-- ----------------------------
INSERT INTO `sys_router`
VALUES (1, 0, '/system', 'SystemManger', '', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"hiddenTag\":false,\"icon\":\"grommet-icons:vm-maintenance\",\"keepAlive\":false,\"rank\":1,\"roles\":[\"1915784833780183043\",\"1915784833780183044\",\"1915784833780183045\",\"1915784833780183046\",\"1915784833796960257\",\"1915784833796960258\",\"1915784833796960259\",\"1915786941359206409\",\"1915786941359206410\"],\"showLink\":true,\"showParent\":true,\"title\":\"systemManagement\",\"transition\":{\"enterTransition\":\"animate__fadeIn\",\"leaveTransition\":\"animate__fadeOut\"}}',
        NULL, 1, '2025-04-28 18:10:16', '2024-09-29 09:46:31', 0);
INSERT INTO `sys_router`
VALUES (2, 1, '/system/menu', 'MenuManger', '/system/menu/index', NULL, 0,
        '{\"activePath\":\"\",\"auths\":[],\"fixedTag\":false,\"hiddenTag\":false,\"icon\":\"subway:menu\",\"keepAlive\":false,\"rank\":5,\"roles\":[\"1915784833796960258\",\"1915784833796960259\"],\"showLink\":true,\"showParent\":true,\"title\":\"system_menu\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        NULL, 1, '2025-04-30 17:11:33', '2024-09-29 09:46:31', 0);
INSERT INTO `sys_router`
VALUES (1841506924681338881, 1844900259930243074, '/configuration/menu-icon', 'MenuIconConfiguration',
        '/configuration/menu-icon/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"subway:menu\",\"keepAlive\":false,\"rank\":2,\"roles\":[\"1916789229485903873\"],\"showLink\":true,\"showParent\":true,\"title\":\"menuIcon\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-28 18:08:01', '2024-10-03 07:53:59', 0);
INSERT INTO `sys_router`
VALUES (1841726844983701505, 1, '/system/role', 'RoleManger', '/system/role/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"fluent-mdl2:manager-self-service\",\"keepAlive\":false,\"rank\":2,\"roles\":[\"1915784833780183046\"],\"showLink\":true,\"showParent\":true,\"title\":\"role\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-30 17:11:19', '2024-10-03 22:27:52', 0);
INSERT INTO `sys_router`
VALUES (1841750734275416065, 1, '/system/power', 'PermissionManger', '/system/permission/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"oui:app-users-roles\",\"keepAlive\":false,\"rank\":3,\"roles\":[\"1915784833780183045\"],\"showLink\":true,\"showParent\":true,\"title\":\"power\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-30 17:11:24', '2024-10-04 00:02:48', 0);
INSERT INTO `sys_router`
VALUES (1841794929635635201, 1844956874037469185, '/element-plus', 'element_plus', '', NULL, 1,
        '{\"auths\":[],\"fixedTag\":false,\"frameLoading\":true,\"frameSrc\":\"https://element-plus.org/zh-CN/\",\"icon\":\"logos:element\",\"keepAlive\":false,\"rank\":62,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"element_plus\",\"transition\":{\"enterTransition\":\"animate__bounce\",\"leaveTransition\":\"animate__rubberBand\"}}',
        1, 1, '2025-04-25 16:34:25', '2024-10-04 02:58:25', 0);
INSERT INTO `sys_router`
VALUES (1841796585525985281, 0, '/iframe', 'external_page', '', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"mdi:iframe-outline\",\"keepAlive\":false,\"rank\":8,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"external_page\",\"transition\":{\"enterTransition\":\"animate__rubberBand\",\"leaveTransition\":\"animate__rubberBand\"}}',
        1, 1, '2025-04-25 16:34:45', '2024-10-04 03:05:00', 0);
INSERT INTO `sys_router`
VALUES (1841796893769580546, 1844956874037469185, '/pure-admin', 'pure_admin', '', NULL, 1,
        '{\"auths\":[],\"fixedTag\":false,\"frameLoading\":true,\"frameSrc\":\"https://pure-admin.cn/\",\"icon\":\"file-icons:pure\",\"keepAlive\":false,\"rank\":63,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"pure_admin\",\"transition\":{\"enterTransition\":\"animate__shakeX\",\"leaveTransition\":\"animate__jello\"}}',
        1, 1, '2025-04-25 16:34:22', '2024-10-04 03:06:13', 0);
INSERT INTO `sys_router`
VALUES (1841803086252548097, 1, '/system/admin-user', 'AdminUserManger', '/system/admin-user/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"ic:round-manage-accounts\",\"keepAlive\":false,\"rank\":1,\"roles\":[\"1915786941359206409\"],\"showLink\":true,\"showParent\":true,\"title\":\"admin_user\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-30 17:11:15', '2024-10-04 03:30:49', 0);
INSERT INTO `sys_router`
VALUES (1842033245832458241, 1, '/system/dept', 'DeptManger', '/system/dept/index', NULL, 0,
        '{\"auths\":[],\"icon\":\"grommet-icons:user-manager\",\"keepAlive\":false,\"rank\":4,\"roles\":[\"1915784833780183043\"],\"showLink\":true,\"showParent\":true,\"title\":\"dept\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-30 17:11:29', '2024-10-04 18:45:24', 0);
INSERT INTO `sys_router`
VALUES (1843932804747603970, 1, '/system/files', 'FileManger', '/system/files/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"line-md:file-filled\",\"keepAlive\":false,\"rank\":6,\"roles\":[\"1915784833780183044\"],\"showLink\":true,\"showParent\":true,\"title\":\"system_files\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-30 17:11:39', '2024-10-10 00:33:34', 0);
INSERT INTO `sys_router`
VALUES (1844276961265557505, 1844900259930243074, '/configuration/email-user', 'EmailUsersConfiguration',
        '/configuration/email-user/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"ic:baseline-voicemail\",\"keepAlive\":false,\"rank\":3,\"roles\":[\"1916789229485903874\"],\"showLink\":true,\"showParent\":true,\"title\":\"emailUsers\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-28 18:08:09', '2024-10-10 23:21:07', 0);
INSERT INTO `sys_router`
VALUES (1844290948342456321, 1844900259930243074, '/configuration/email-template', 'EmailTemplate',
        '/configuration/email-template/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"entypo:email\",\"keepAlive\":false,\"rank\":5,\"roles\":[\"1916789229485903876\"],\"showLink\":true,\"showParent\":true,\"title\":\"emailTemplate\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-28 18:08:17', '2024-10-11 00:16:42', 0);
INSERT INTO `sys_router`
VALUES (1844644093987880962, 0, '/monitor', 'Monitor', '', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"carbon:cloud-monitoring\",\"keepAlive\":false,\"rank\":3,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"monitor\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-26 09:30:47', '2024-10-11 23:39:58', 0);
INSERT INTO `sys_router`
VALUES (1844644779039358978, 1844644093987880962, '/monitor/server', 'MonitorServer', '/monitor/server/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"mingcute:server-fill\",\"keepAlive\":false,\"rank\":3,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"monitoring_server\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-26 09:30:52', '2024-10-11 23:42:42', 0);
INSERT INTO `sys_router`
VALUES (1844900259930243074, 0, '/configuration', 'SystemConfiguration', '', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"hugeicons:configuration-01\",\"keepAlive\":false,\"rank\":2,\"roles\":[\"1916789229485903873\",\"1916789229485903874\",\"1916789229485903876\"],\"showLink\":true,\"showParent\":true,\"title\":\"configuration\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-05-06 20:53:31', '2024-10-12 16:37:53', 0);
INSERT INTO `sys_router`
VALUES (1844956874037469185, 1841796585525985281, '/iframe/embedded-doc', 'embedded_doc', '', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"mdi:iframe-braces\",\"keepAlive\":false,\"rank\":9,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"embedded_doc\",\"transition\":{\"enterTransition\":\"animate__bounce\",\"leaveTransition\":\"animate__flash\"}}',
        1, 1, '2025-04-25 16:34:28', '2024-10-12 20:22:51', 0);
INSERT INTO `sys_router`
VALUES (1844957189138751490, 1841796585525985281, '/ifram/external-doc', 'external_doc', '', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"line-md:link\",\"keepAlive\":false,\"rank\":8,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"external_doc\",\"transition\":{\"enterTransition\":\"animate__flash\",\"leaveTransition\":\"animate__flash\"}}',
        1, 1, '2025-04-25 16:34:42', '2024-10-12 20:24:06', 0);
INSERT INTO `sys_router`
VALUES (1844957830590468097, 1844957189138751490, '/external-doc/element-plus',
        'https://element-plus.org/zh-CN/component/overview.html', '', NULL, 2,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"logos:element\",\"keepAlive\":true,\"rank\":9,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"element_plus\",\"transition\":{\"enterTransition\":\"animate__bounceInRight\",\"leaveTransition\":\"animate__bounceInRight\"}}',
        1, 1, '2025-04-25 16:34:32', '2024-10-12 20:26:39', 0);
INSERT INTO `sys_router`
VALUES (1844958437262987265, 1844957189138751490, '/external-doc/iconify', 'https://icon-sets.iconify.design/', '',
        NULL, 2,
        '{\"auths\":[],\"icon\":\"line-md:iconify1\",\"rank\":8,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"iconify\",\"transition\":{\"enterTransition\":\"animate__bounceInRight\",\"leaveTransition\":\"fade\"}}',
        1, 1, '2025-04-25 16:34:35', '2024-10-12 20:29:04', 0);
INSERT INTO `sys_router`
VALUES (1845812113861079042, 1846804024660791298, '/scheduler/schedulers', 'SchedulerTask',
        '/scheduler/schedulers/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"simple-icons:apachedolphinscheduler\",\"keepAlive\":false,\"rank\":6,\"roles\":[\"1916789229485903879\"],\"showLink\":true,\"showParent\":true,\"title\":\"schedulers\",\"transition\":{\"enterTransition\":\"animate__fadeInUp\",\"leaveTransition\":\"animate__fadeInDown\"}}',
        1, 1, '2025-04-28 18:32:06', '2024-10-15 05:01:16', 0);
INSERT INTO `sys_router`
VALUES (1846166163060285441, 1846804024660791298, '/scheduler/schedulers-group', 'SchedulerGroup',
        '/scheduler/schedulers-group/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"uis:layer-group\",\"keepAlive\":false,\"rank\":7,\"roles\":[\"1916789229485903880\"],\"showLink\":true,\"showParent\":true,\"title\":\"schedulersGroup\",\"transition\":{\"enterTransition\":\"animate__fadeInUp\",\"leaveTransition\":\"animate__fadeInDown\"}}',
        1, 1, '2025-04-28 18:32:14', '2024-10-16 04:28:08', 0);
INSERT INTO `sys_router`
VALUES (1846804024660791298, 0, '/scheduler', 'Scheduler', '', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"mingcute:time-fill\",\"keepAlive\":false,\"rank\":4,\"roles\":[\"1916789229485903879\",\"1916789229485903880\"],\"showLink\":true,\"showParent\":true,\"title\":\"scheduler\",\"transition\":{\"enterTransition\":\"animate__fadeInUp\",\"leaveTransition\":\"animate__fadeInDown\"}}',
        1, 1, '2025-04-28 18:31:59', '2024-10-17 14:42:46', 0);
INSERT INTO `sys_router`
VALUES (1847140225619992577, 1852321196101464065, '/log/scheduler-execute-log', 'SchedulerExecuteLog',
        '/monitor/scheduler-execute-log/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"eos-icons:cronjob\",\"keepAlive\":false,\"rank\":6,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"quartzExecuteLog\",\"transition\":{\"enterTransition\":\"animate__fadeIn\",\"leaveTransition\":\"animate__tada\"}}',
        1, 1, '2025-04-26 09:36:47', '2024-10-18 12:58:43', 0);
INSERT INTO `sys_router`
VALUES (1847291834822123521, 1852321196101464065, '/log/user-login-log', 'UserLoginLog',
        '/monitor/user-login-log/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"ph:clock-user\",\"keepAlive\":false,\"rank\":7,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"userLoginLog\",\"transition\":{\"enterTransition\":\"animate__fadeIn\",\"leaveTransition\":\"animate__tada\"}}',
        1, 1, '2025-04-26 09:36:51', '2024-10-18 23:01:09', 0);
INSERT INTO `sys_router`
VALUES (1848989760243838978, 1844644093987880962, '/monitor/caches', 'SystemCaches', '/monitor/caches/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"devicon:redis\",\"keepAlive\":false,\"rank\":4,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"systemCaches\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-26 09:30:59', '2024-10-23 15:28:06', 0);
INSERT INTO `sys_router`
VALUES (1849000501604724738, 1844900259930243074, '/configuration/web-configuration', 'WebConfiguration',
        '/configuration/web-configuration/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"file-icons:config-react\",\"keepAlive\":false,\"rank\":4,\"roles\":[\"1916789229485903875\"],\"showLink\":true,\"showParent\":true,\"title\":\"webConifg\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-28 18:08:28', '2024-10-23 16:10:47', 0);
INSERT INTO `sys_router`
VALUES (1851488898978103297, 0, '/message', 'MessageManger', '', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"line-md:email-filled\",\"keepAlive\":false,\"rank\":7,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"messageManagement\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1849444494908125181, 1, '2025-04-26 09:36:56', '2024-10-30 12:58:47', 0);
INSERT INTO `sys_router`
VALUES (1851488972810436609, 1851488898978103297, '/message/message-type', 'MessageType',
        '/message-manger/message-type/index', NULL, 0,
        '{\"auths\":[],\"icon\":\"hugeicons:message-edit-02\",\"rank\":7,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"messageType\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1849444494908125181, 1, '2025-04-26 09:37:01', '2024-10-30 12:59:05', 0);
INSERT INTO `sys_router`
VALUES (1851490002939887618, 0, '/systemMaintenance', 'systemMaintenance', '', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"simple-icons:minio\",\"keepAlive\":false,\"rank\":10,\"roles\":[],\"showLink\":false,\"showParent\":true,\"title\":\"systemMaintenance\",\"transition\":{\"enterTransition\":\"animate__bounceInRight\",\"leaveTransition\":\"animate__bounceInLeft\"}}',
        1, 1, '2025-04-25 16:34:18', '2024-10-30 13:03:10', 0);
INSERT INTO `sys_router`
VALUES (1851491818972856321, 1851488898978103297, '/message/message-editing', 'MessageEditer',
        '/message-manger/message-editing/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"hugeicons:message-edit-02\",\"keepAlive\":false,\"rank\":9,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"messageEditing\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-26 09:37:11', '2024-10-30 13:10:23', 0);
INSERT INTO `sys_router`
VALUES (1851525168378875906, 1851488898978103297, '/message/message-received', 'MessageReceived',
        '/message-manger/message-received/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"wpf:gps-receiving\",\"keepAlive\":false,\"rank\":10,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"messageReceivingManagement\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-26 09:37:15', '2024-10-30 15:22:54', 0);
INSERT INTO `sys_router`
VALUES (1852321196101464065, 0, '/logManagement', 'logManagement', '', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"octicon:log-16\",\"keepAlive\":false,\"rank\":6,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"logManagement\",\"transition\":{\"enterTransition\":\"animate__fadeIn\",\"leaveTransition\":\"animate__tada\"}}',
        1, 1, '2025-04-26 09:36:43', '2024-11-01 20:06:02', 0);
INSERT INTO `sys_router`
VALUES (1853083388413304834, 1851488898978103297, '/message/message-send', 'MessageSender',
        '/message-manger/message-send/index', NULL, 0,
        '{\"auths\":[],\"fixedTag\":false,\"icon\":\"lets-icons:message-alt-fill\",\"keepAlive\":false,\"rank\":8,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"messageSendManagement\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-04-26 09:37:05', '2024-11-03 22:34:43', 0);
INSERT INTO `sys_router`
VALUES (1918937765434429441, 1844644093987880962, '/monitor/logged-in', 'MonitorLoggedIn',
        '/monitor/logged-in/index.vue', '', 0,
        '{\"activePath\":\"\",\"auths\":[],\"fixedTag\":false,\"frameLoading\":true,\"frameSrc\":\"\",\"hiddenTag\":false,\"icon\":\"material-symbols:login\",\"keepAlive\":false,\"rank\":5,\"roles\":[],\"showLink\":true,\"showParent\":true,\"title\":\"logged_in_user\",\"transition\":{\"enterTransition\":\"\",\"leaveTransition\":\"\"}}',
        1, 1, '2025-05-04 19:34:26', '2025-05-04 15:56:30', 0);

ALTER TABLE `sys_router`
    ADD COLUMN `status` tinyint NULL DEFAULT 0 COMMENT '1:禁用 0:正常' AFTER `meta`;

-- ----------------------------
-- Table structure for sys_router_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_router_role`;
CREATE TABLE `sys_router_role`
(
    `id`          bigint                       NOT NULL COMMENT '主键ID',
    `router_id`   bigint                       NOT NULL COMMENT '路由ID',
    `role_id`     bigint                       NOT NULL COMMENT '角色ID',
    `create_time` datetime                     NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改的时间戳',
    `create_user` bigint                       NOT NULL COMMENT '创建用户',
    `update_user` bigint                       NULL     DEFAULT NULL COMMENT '操作用户',
    `is_deleted`  tinyint(1) UNSIGNED ZEROFILL NOT NULL DEFAULT 0 COMMENT '是否被删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_router_role` (`router_id` ASC, `role_id` ASC) USING BTREE COMMENT '录音和角色不能重复',
    INDEX `idx_router_id` (`router_id` ASC) USING BTREE,
    INDEX `idx_role_id` (`role_id` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间',
    CONSTRAINT `sys_router_role_ibfk_1` FOREIGN KEY (`router_id`) REFERENCES `sys_router` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `sys_router_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = '系统路由角色关系表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_router_role
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`          bigint                                                        NOT NULL COMMENT 'ID',
    `username`    varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '用户名',
    `nickname`    varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '昵称',
    `email`       varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
    `phone`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '手机号',
    `password`    varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
    `avatar`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `sex`         tinyint                                                       NULL DEFAULT 1 COMMENT '0:女 1:男',
    `summary`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人描述',
    `ip_address`  varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '最后登录IP',
    `ip_region`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录ip归属地',
    `status`      tinyint                                                       NULL DEFAULT 0 COMMENT '1:禁用 0:正常',
    `create_time` datetime                                                      NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime                                                      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                                                        NULL DEFAULT NULL COMMENT '创建用户',
    `update_user` bigint                                                        NULL DEFAULT NULL COMMENT '操作用户',
    `is_deleted`  tinyint                                                       NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_username` (`username` ASC) USING BTREE COMMENT '用户名不能重复',
    UNIQUE INDEX `unique_email` (`email` ASC) USING BTREE COMMENT '邮箱不能重复',
    INDEX `idx_nickname` (`nickname` ASC) USING BTREE,
    INDEX `idx_phone` (`phone` ASC) USING BTREE,
    INDEX `idx_avatar` (`avatar` ASC) USING BTREE,
    INDEX `idx_summary` (`summary` ASC) USING BTREE,
    INDEX `idx_ip_address` (`ip_address` ASC) USING BTREE,
    INDEX `idx_ip_region` (`ip_region` ASC) USING BTREE,
    INDEX `idx_status` (`status` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间'
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user`
VALUES (1, 'Administrator', 'Administrator', 'admin@qq.com', '1864692046',
        '$2a$10$gD2mTVaqIjfgciN.5vQ7uemf.PYNYLFQL4BZdnYswZEjAuC543Vhe',
        NULL, 1, 'admin', '127.0.0.1', '内网IP', 0,
        '2024-10-24 21:35:03', '2025-05-09 17:34:32', 1, 1, 0);
INSERT INTO `sys_user`
VALUES (1849444494908125181, 'bunny', 'bunny', '1319900154@qq.com', '18012062876',
        '$2a$10$h5BUwmMaVcEuu7Bz0TPPy.PQV8JP6CFJlbHTgT78G1s0YPIu2kfXe',
        NULL, 1, 'bunny', '127.0.0.1', '内网IP', 0,
        '2024-09-26 14:29:33', '2025-05-09 12:22:28', 1849444494908125181, 1, 0);
INSERT INTO `sys_user`
VALUES (1849681227633758210, 'Operation', '系统配置', 'Operation@qq.com', '18012345678',
        '$2a$10$h5BUwmMaVcEuu7Bz0TPPy.PQV8JP6CFJlbHTgT78G1s0YPIu2kfXe', NULL, 0,
        '能看到定时任务和系统配置页面可以发布和更新消息，密码：admin123', '127.0.0.1', '内网IP', 0, '2024-10-25 13:15:45',
        '2025-05-09 12:17:51', 1, 1, 0);
INSERT INTO `sys_user`
VALUES (1850075157831454722, 'system', '只能看到系统配置用户1', 'system@Gmail.com', '18012062876',
        '$2a$10$h5BUwmMaVcEuu7Bz0TPPy.PQV8JP6CFJlbHTgT78G1s0YPIu2kfXe', NULL, 0,
        '只能看到系统设置1内容页面，密码：admin123', '113.201.133.129', '陕西省,西安市 联通', 0, '2024-10-26 15:21:05',
        '2025-05-09 12:17:51', 1, 1849681227633758210, 1);
INSERT INTO `sys_user`
VALUES (1850080272764211202, 'timing', '定时任务', 'timing@163.com', '212122',
        '$2a$10$h5BUwmMaVcEuu7Bz0TPPy.PQV8JP6CFJlbHTgT78G1s0YPIu2kfXe', NULL, 0, '只能看到定时任务页面，密码：admin123',
        '127.0.0.1', '内网IP', 0, '2024-10-26 15:41:25', '2025-05-09 12:17:51', 1, NULL, 0);
INSERT INTO `sys_user`
VALUES (1853494274437152770, 'test', 'test', 'test@qq.com', '18012062876',
        '$2a$10$h5BUwmMaVcEuu7Bz0TPPy.PQV8JP6CFJlbHTgT78G1s0YPIu2kfXe', NULL, 0, 'test', '127.0.0.1', '内网IP', 0,
        '2024-11-05 01:47:26', '2025-02-22 21:21:06', 1, 1, 1);

-- ----------------------------
-- Table structure for sys_user_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_dept`;
CREATE TABLE `sys_user_dept`
(
    `id`          bigint                       NOT NULL COMMENT 'ID',
    `user_id`     bigint                       NOT NULL COMMENT '用户id',
    `dept_id`     bigint                       NOT NULL COMMENT '部门id',
    `create_time` datetime                     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                       NOT NULL COMMENT '创建用户',
    `update_user` bigint                       NULL DEFAULT NULL COMMENT '更新用户',
    `is_deleted`  tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '是否删除，0-未删除，1-已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_user_dept` (`user_id` ASC, `dept_id` ASC) USING BTREE COMMENT '用户id和部门不能相同',
    INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
    INDEX `idx_dept_id` (`dept_id` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间',
    CONSTRAINT `sys_user_dept_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `sys_user_dept_ibfk_2` FOREIGN KEY (`dept_id`) REFERENCES `sys_dept` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT = '部门用户关系表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_dept
-- ----------------------------
INSERT INTO `sys_user_dept`
VALUES (1849443729225383937, 1, 1842883239493881857, '2024-10-24 21:32:01', '2024-10-24 21:32:01', 1, 1, 0);
INSERT INTO `sys_user_dept`
VALUES (1850080272827125761, 1850080272764211202, 1850077710275153922, '2024-10-26 15:41:25', '2024-10-26 15:41:25', 1,
        1, 0);
INSERT INTO `sys_user_dept`
VALUES (1916796760199364610, 1849681227633758210, 1842885831187877890, '2025-04-28 18:08:55', '2025-04-28 18:08:55', 1,
        1, 0);
INSERT INTO `sys_user_dept`
VALUES (1920695839786622977, 1849444494908125181, 1842844360640327682, '2025-05-09 12:22:28', '2025-05-09 12:22:28', 1,
        1, 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `id`          bigint                       NOT NULL COMMENT 'ID',
    `user_id`     bigint                       NOT NULL COMMENT '用户id',
    `role_id`     bigint                       NOT NULL COMMENT '角色id',
    `create_time` timestamp                    NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp                    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user` bigint                       NOT NULL COMMENT '创建用户',
    `update_user` bigint                       NULL DEFAULT NULL COMMENT '更新用户',
    `is_deleted`  tinyint(1) UNSIGNED ZEROFILL NULL DEFAULT 0 COMMENT '是否删除，0-未删除，1-已删除',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `idx_user_role` (`user_id` ASC, `role_id` ASC) USING BTREE COMMENT '用户和角色不能相同',
    INDEX `idx_user_id` (`user_id` ASC) USING BTREE,
    INDEX `idx_role_id` (`role_id` ASC) USING BTREE,
    INDEX `idx_update_user` (`update_user` ASC) USING BTREE COMMENT '索引创更新用户',
    INDEX `idx_create_user` (`create_user` ASC) USING BTREE COMMENT '索引创建用户',
    INDEX `idx_user` (`update_user` ASC, `create_user` ASC) USING BTREE COMMENT '索引创建用户和更新用户',
    INDEX `idx_time` (`update_time` ASC, `create_time` ASC) USING BTREE COMMENT '索引创建时间和更新时间',
    CONSTRAINT `sys_user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `sys_user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户角色关系表'
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role`
VALUES (1955000000000000301, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1, 0);

-- ----------------------------
-- View structure for view_qrtz_schedulers
-- ----------------------------
DROP VIEW IF EXISTS `view_qrtz_schedulers`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `view_qrtz_schedulers` AS
select `job`.`job_name`         AS `job_name`,
       `job`.`job_group`        AS `job_group`,
       `job`.`description`      AS `description`,
       `job`.`job_class_name`   AS `job_class_name`,
       `cron`.`cron_expression` AS `cron_expression`,
       `tri`.`trigger_name`     AS `trigger_name`,
       `tri`.`trigger_state`    AS `trigger_state`
from ((`qrtz_job_details` `job` join `qrtz_triggers` `tri` on (((`job`.`job_name` = `tri`.`job_name`) and
                                                                (`job`.`job_group` = `tri`.`job_group`)))) join `qrtz_cron_triggers` `cron`
      on (((`cron`.`trigger_name` = `tri`.`trigger_name`) and (`cron`.`trigger_group` = `tri`.`job_group`))))
where (`tri`.`trigger_type` = 'CRON');

SET FOREIGN_KEY_CHECKS = 1;
