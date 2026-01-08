# Codex 计划

## 需求（已确认）

- 建设 4 个独立后端：edu-app、edu-admin、social-app、social-admin。
- 单库，按表前缀区分：edu_、social_、core_。
- core 表覆盖用户、学校与平台配置；所有 core 表包含 edu_enabled、social_enabled 字段（不以 is 开头）。
- 管理端表完全隔离（edu-admin 与 social-admin），暂不纳入本次范围。
- IM 当前不实现，仅保留扩展接口以便后续接入。
- 不迁移历史数据（全新建库建表）。
- 二手书/订单模块暂不考虑。
- 教务与社交昵称/头像保持一致。
- 保留后台管理员到用户的映射（sys_user.xt_user_id）。
- user_push_set 拆分为 edu_user_push_set 与 social_user_push_set。
- user_check 与 user_check_config 归属 social。
- 技术基线：JDK 25+ 与 Spring Boot 4.x。

## 架构概览

- 单仓多应用（多个 Spring Boot 应用 + 共享代码库）。
- common 为共享库模块（jar），供所有应用复用以避免重复。
- 应用相互独立，不要求服务间通信。
- 注册中心/网关为可选项，建议暂时移除。

建议目录结构：

- common/
- edu-app/
- edu-admin/
- social-app/
- social-admin/

## 模块迁移计划（按模块）

### 来自 xt-school（小程序后端）

- xt-common -> common（共享配置、工具、基础实体、异常处理、分页、安全辅助）。
- xt-feign -> 移除（目标架构不需要跨服务 RPC）。
- xt-gateway -> 移除（4 个独立应用不需要网关）。
- xt-module/xt-user -> 拆分：
    - core-user：core_user、core_user_wx_mp 与 core_platform_*（共享平台配置，含 edu_enabled/social_enabled）。
    - edu-user：edu_user_push_set（教务通知设置）。
    - social-user：钱包/积分/关注/匿名、social_user_check(_config)、social_user_push_set。
- xt-module/xt-study -> edu-app（教务域全部功能）。
- xt-module/xt-circle -> social-app。
- xt-module/xt-message -> social-app（IM 关闭，仅保留通知逻辑）。
- xt-module/xt-takeout -> social-app。
- xt-module/xt-book -> 暂缓（不在范围）。
- xt-module/xt-plugin -> 暂缓，若仍使用可并入 common。
- xt-module/xt-log -> common 或各应用独立日志模块。

### 来自 xt-agent（管理端后端）

- agent-auth -> 拆分为 edu-admin-auth 与 social-admin-auth。
- agent-system -> 拆分为 edu-admin-system 与 social-admin-system（RBAC 隔离）。
- agent-user -> 拆分为 edu-admin-user 与 social-admin-user。
- agent-study -> edu-admin。
- agent-circle -> social-admin。
- agent-msg -> social-admin（IM 关闭）。
- agent-takeout -> social-admin。
- agent-log -> 管理端日志。
- agent-gateway -> 移除。
- agent-api/feign -> 移除。

### 前端

- wxy -> 拆分为教务小程序前端与社交小程序前端。
- wxy-admin -> 替换为两套 pure-admin 前端（edu-admin 与 social-admin）。

## 数据库方案（字段级）

### Core（用户、学校、平台配置）

- core_user (来源 xt_user.user_info) ✔️

| 字段               | 类型                           |
|------------------|------------------------------|
| id               | bigint                       |
| school_code      | varchar(100)                 |
| im_id            | varchar(100)                 |
| im_token         | varchar(255)                 |
| parent_id        | bigint                       |
| invite_time      | datetime                     |
| nick_name        | varchar(100)                 |
| ma_open_id       | varchar(150)                 |
| mp_open_id       | varchar(150)                 |
| union_id         | varchar(255)                 |
| avatar_url       | varchar(255)                 |
| back_url         | varchar(255)                 |
| invite_code      | varchar(50)                  |
| phone_number     | varchar(100)                 |
| birthday         | datetime                     |
| country          | varchar(50)                  |
| province         | varchar(50)                  |
| city             | varchar(50)                  |
| gender           | tinyint                      |
| remark           | varchar(255)                 |
| edu_enabled      | tinyint(1)                   |
| social_enabled   | tinyint(1)                   |
| is_disable       | tinyint(1)                   |
| is_destroy       | tinyint(1)                   |
| is_fake          | tinyint(1) unsigned zerofill |
| view_school_code | varchar(255)                 |
| is_muted         | tinyint(1) unsigned zerofill |
| last_login_time  | datetime                     |
| new_usage_time   | datetime                     |
| create_time      | datetime                     |
| update_time      | datetime                     |

- 备注：im_id/im_token 保持可空，便于未来接入 IM。

- core_user_wx_mp (来源 xt_user.user_wx_mp) ✔️

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| mp_open_id     | varchar(150) |
| union_id       | varchar(255) |
| status         | tinyint      |
| edu_enabled    | tinyint(1)   |
| social_enabled | tinyint(1)   |
| create_time    | datetime     |
| update_time    | datetime     |
| cancel_time    | datetime     |

- core_school (来源 xt_study.school_info) ✔️

| 字段                  | 类型           |
|---------------------|--------------|
| id                  | bigint       |
| school_name         | varchar(50)  |
| simple_name         | varchar(50)  |
| school_code         | varchar(255) |
| school_logo         | varchar(150) |
| remark              | varchar(255) |
| current_term        | varchar(100) |
| current_week        | int          |
| start_date          | varchar(100) |
| auth_tip            | varchar(150) |
| agent_status        | tinyint(1)   |
| agent_time          | datetime     |
| agent_id            | bigint       |
| agent_vx            | varchar(255) |
| school_invite_code  | varchar(255) |
| status              | tinyint      |
| expected_open_count | int          |
| open_progress       | int          |
| open_time           | datetime     |
| college             | json         |
| introduction        | text         |
| build_time          | varchar(20)  |
| school_level        | varchar(20)  |
| school_attributes   | varchar(20)  |
| school_pro          | json         |
| campus_count        | int          |
| initial             | varchar(10)  |
| school_type         | varchar(10)  |
| edu_enabled         | tinyint(1)   |
| social_enabled      | tinyint(1)   |
| create_time         | datetime     |
| update_time         | datetime     |

- core_school_campus (来源 xt_study.school_info_campus) ✔️

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| school_code    | varchar(50)  |
| campus_code    | varchar(50)  |
| campus_name    | varchar(100) |
| campus_cover   | varchar(150) |
| province       | varchar(100) |
| city           | varchar(100) |
| location_x     | varchar(255) |
| location_y     | varchar(255) |
| location       | point        |
| campus_area    | int          |
| school_mark    | decimal(10   |
| 2)             |              |
| trans_mark     | decimal(10   |
| 2)             |              |
| build_mark     | decimal(10   |
| 2)             |              |
| env_mark       | decimal(10   |
| 2)             |              |
| eat_mark       | decimal(10   |
| 2)             |              |
| dorm_mark      | decimal(10   |
| 2)             |              |
| edu_enabled    | tinyint(1)   |
| social_enabled | tinyint(1)   |
| create_time    | datetime     |
| update_time    | datetime     |

- core_platform_banner (来源 xt_user.platform_banner) ❌

| 字段             | 类型                           |
|----------------|------------------------------|
| id             | bigint                       |
| sort           | int                          |
| school_code    | varchar(255)                 |
| is_open        | tinyint                      |
| is_official    | tinyint(1) unsigned zerofill |
| banner_url     | varchar(255)                 |
| close_url      | varchar(255)                 |
| redirect_url   | varchar(255)                 |
| pop_url        | varchar(255)                 |
| app_id         | varchar(255)                 |
| click_type     | tinyint(1)                   |
| click_tip      | varchar(255)                 |
| is_del         | tinyint(1) unsigned zerofill |
| use_place      | tinyint(1)                   |
| edu_enabled    | tinyint(1)                   |
| social_enabled | tinyint(1)                   |
| create_time    | datetime                     |
| update_time    | datetime                     |

- core_platform_emoji (来源 xt_user.platform_emoji) ❌

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| emoji_url      | text         |
| notes          | varchar(255) |
| sort           | int          |
| edu_enabled    | tinyint(1)   |
| social_enabled | tinyint(1)   |
| create_time    | datetime     |
| update_time    | datetime     |

- core_platform_income (来源 xt_user.platform_income)

| 字段             | 类型         |
|----------------|------------|
| id             | bigint     |
| user_id        | bigint     |
| income_num     | decimal(10 |
| 2)             |            |
| income_type    | tinyint(1) |
| edu_enabled    | tinyint(1) |
| social_enabled | tinyint(1) |
| create_time    | datetime   |
| update_time    | datetime   |

- core_platform_menu_button (来源 xt_user.platform_menu_button) ❌

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| button_place   | tinyint(1)   |
| button_name    | varchar(255) |
| button_desc    | varchar(255) |
| icon           | varchar(255) |
| is_official    | tinyint(1)   |
| is_del         | tinyint(1)   |
| edu_enabled    | tinyint(1)   |
| social_enabled | tinyint(1)   |
| create_time    | datetime     |
| update_time    | datetime     |

- core_platform_menu_button_school (来源 xt_user.platform_menu_button_school) ❌

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| school_code    | varchar(20)  |
| button_id      | bigint       |
| button_rename  | varchar(100) |
| is_open        | tinyint      |
| redirect_url   | varchar(255) |
| pop_url        | varchar(255) |
| app_id         | varchar(255) |
| click_type     | tinyint      |
| click_tip      | varchar(255) |
| is_del         | tinyint      |
| sort           | int          |
| edu_enabled    | tinyint(1)   |
| social_enabled | tinyint(1)   |
| create_time    | datetime     |
| update_time    | datetime     |

- core_platform_notice (来源 xt_user.platform_notice) ✔️

| 字段             | 类型           |
|----------------|--------------|
| id             | int          |
| redirect_url   | varchar(255) |
| type           | tinyint(1)   |
| app_id         | varchar(255) |
| notice         | varchar(255) |
| is_show        | tinyint(1)   |
| school_code    | varchar(100) |
| place          | tinyint(1)   |
| is_official    | tinyint(1)   |
| is_del         | tinyint(1)   |
| edu_enabled    | tinyint(1)   |
| social_enabled | tinyint(1)   |
| create_time    | datetime     |
| update_time    | datetime     |

- core_platform_pop_ad (来源 xt_user.platform_pop_ad) ❌

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| click_type     | tinyint      |
| click_tip      | varchar(255) |
| notes          | varchar(255) |
| pop_path       | varchar(255) |
| pop_img        | varchar(255) |
| app_id         | varchar(255) |
| is_show        | tinyint      |
| place          | tinyint(1)   |
| interval_time  | int          |
| pop_type       | tinyint(1)   |
| is_official    | tinyint(1)   |
| is_del         | tinyint(1)   |
| edu_enabled    | tinyint(1)   |
| social_enabled | tinyint(1)   |
| create_time    | datetime     |
| update_time    | datetime     |

- core_platform_pop_ad_link (来源 xt_user.platform_pop_ad_link) ❌

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| pop_id         | bigint       |
| school_code    | varchar(100) |
| is_del         | tinyint(1)   |
| is_show        | tinyint(1)   |
| edu_enabled    | tinyint(1)   |
| social_enabled | tinyint(1)   |
| create_time    | datetime     |
| update_time    | datetime     |

- core_platform_task (来源 xt_user.platform_task) ❌

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| task_icon      | varchar(255) |
| task_title     | varchar(100) |
| task_desc      | varchar(255) |
| task_type      | tinyint(1)   |
| task_num       | int          |
| task_coin_num  | int          |
| task_coin_type | tinyint(1)   |
| is_del         | tinyint(1)   |
| edu_enabled    | tinyint(1)   |
| social_enabled | tinyint(1)   |
| create_time    | datetime     |
| update_time    | datetime     |

- core_platform_task_receive (来源 xt_user.platform_task_receive) ❌

| 字段             | 类型         |
|----------------|------------|
| id             | bigint     |
| task_id        | bigint     |
| user_id        | bigint     |
| is_complete    | tinyint(1) |
| is_receive     | tinyint(1) |
| edu_enabled    | tinyint(1) |
| social_enabled | tinyint(1) |
| create_time    | datetime   |
| update_time    | datetime   |

- core_platform_version (来源 xt_user.platform_version) ❌

| 字段             | 类型         |
|----------------|------------|
| id             | int        |
| token_version  | int        |
| ea_version     | int        |
| book_version   | decimal(10 |
| 2)             |            |
| edu_enabled    | tinyint(1) |
| social_enabled | tinyint(1) |

### 教务域（edu_*）

- edu_school_calendar (来源 xt_study.school_calendar)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(100) |
| title       | text         |
| term        | varchar(100) |
| event_time  | text         |
| create_time | datetime     |
| update_time | datetime     |

- edu_school_dept_phone (来源 xt_study.school_dept_phone)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(20)  |
| dept_name   | varchar(100) |
| dept_phone  | varchar(150) |
| create_time | datetime     |
| update_time | datetime     |

- edu_school_emptyroom (来源 xt_study.school_emptyroom)

| 字段          | 类型             |
|-------------|----------------|
| id          | bigint         |
| school_code | varchar(100)   |
| campus_code | varchar(100)   |
| building    | varchar(100)   |
| week        | tinyint        |
| day         | tinyint        |
| term        | varbinary(100) |
| create_time | datetime       |
| update_time | datetime       |

- edu_school_emptyroom_detail (来源 xt_study.school_emptyroom_detail)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| emptyroom_id | bigint       |
| school_code  | varchar(100) |
| campus_code  | varchar(100) |
| free_time    | varchar(100) |
| free_room    | text         |
| create_time  | datetime     |
| update_time  | datetime     |

- edu_school_info_config (来源 xt_study.school_info_config)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| school_code  | varchar(50)  |
| system_type  | tinyint      |
| system_label | varchar(100) |
| create_time  | datetime     |
| update_time  | datetime     |

- edu_school_info_feature (来源 xt_study.school_info_feature)

| 字段                    | 类型          |
|-----------------------|-------------|
| id                    | bigint      |
| school_code           | varchar(50) |
| class_schedule_open   | tinyint(1)  |
| teacher_schedule_open | tinyint(1)  |
| room_schedule_open    | tinyint(1)  |
| gpa_prediction_open   | tinyint(1)  |
| gpa_rank_open         | tinyint     |
| gpa_rank_open_count   | int         |
| create_time           | datetime    |
| update_time           | datetime    |

- edu_school_info_img (来源 xt_study.school_info_img)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(50)  |
| campus_code | varchar(50)  |
| img_type    | varchar(20)  |
| url_label   | varchar(100) |
| img_url     | varchar(255) |
| create_time | datetime     |
| update_time | datetime     |

- edu_school_room_addr (来源 xt_study.school_room_addr)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(255) |
| parent_id   | bigint       |
| term        | varchar(100) |
| label       | varchar(150) |
| create_time | datetime     |
| update_time | datetime     |

- edu_school_tree_class (来源 xt_study.school_tree_class)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(100) |
| term        | varchar(100) |
| department  | varchar(255) |
| grade       | varchar(255) |
| major       | varchar(255) |
| class_list  | json         |
| create_time | datetime     |
| update_time | datetime     |

- edu_school_tree_classroom (来源 xt_study.school_tree_classroom)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(100) |
| campus      | varchar(255) |
| term        | varchar(100) |
| floor       | varchar(255) |
| rooms       | json         |
| create_time | datetime     |
| update_time | datetime     |

- edu_school_tree_teacher (来源 xt_study.school_tree_teacher)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(100) |
| term        | varchar(100) |
| college     | varchar(255) |
| teachers    | json         |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_auth_img (来源 xt_study.student_auth_img)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| user_id     | bigint       |
| auth_image  | text         |
| reason      | varchar(255) |
| status      | tinyint(1)   |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_course (来源 xt_study.student_course)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(50)  |
| student_id  | bigint       |
| user_id     | bigint       |
| course_name | varchar(255) |
| teacher     | varchar(255) |
| term        | varchar(100) |
| sort        | int          |
| room        | varchar(255) |
| day         | int          |
| week        | text         |
| week_desc   | varchar(200) |
| note        | text         |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_course_arow (来源 xt_study.student_course_arow)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(50)  |
| student_id  | bigint       |
| user_id     | bigint       |
| course_name | varchar(255) |
| teacher     | varchar(255) |
| term        | varchar(100) |
| sort        | int          |
| room        | varchar(255) |
| day         | int          |
| week        | text         |
| week_desc   | varchar(200) |
| note        | text         |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_course_class (来源 xt_study.student_course_class)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(50)  |
| term        | varchar(100) |
| department  | varchar(255) |
| grade       | varchar(255) |
| major       | varchar(255) |
| class_type  | varchar(255) |
| course_name | varchar(255) |
| teacher     | varchar(255) |
| sort        | int          |
| room        | varchar(255) |
| day         | int          |
| week        | json         |
| week_desc   | varchar(200) |
| note        | text         |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_course_class_time (来源 xt_study.student_course_class_time)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(100) |
| department  | varchar(255) |
| major       | varchar(255) |
| grade       | varchar(255) |
| class_type  | varchar(255) |
| begin_time  | varchar(100) |
| end_time    | varchar(100) |
| term        | varchar(100) |
| sort        | int          |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_course_classroom (来源 xt_study.student_course_classroom)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(50)  |
| campus      | varchar(255) |
| floor       | varchar(255) |
| room        | varchar(255) |
| term        | varchar(100) |
| day         | int          |
| course_name | varchar(255) |
| teacher     | varchar(255) |
| sort        | int          |
| class_type  | varchar(255) |
| week        | json         |
| week_desc   | varchar(200) |
| note        | text         |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_course_classroom_time (来源 xt_study.student_course_classroom_time)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(100) |
| campus      | varchar(255) |
| floor       | varchar(255) |
| room        | varchar(255) |
| term        | varchar(100) |
| begin_time  | varchar(100) |
| end_time    | varchar(100) |
| sort        | int          |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_course_share (来源 xt_study.student_course_share)

| 字段              | 类型           |
|-----------------|--------------|
| id              | bigint       |
| send_user_id    | bigint       |
| receive_user_id | bigint       |
| send_nick       | varchar(255) |
| receive_nick    | varchar(255) |
| send_sort       | tinyint      |
| receive_sort    | tinyint      |
| status          | tinyint(1)   |
| create_time     | datetime     |
| update_time     | datetime     |

- edu_student_course_sorts (来源 xt_study.student_course_sorts)

| 字段          | 类型       |
|-------------|----------|
| id          | bigint   |
| user_id     | bigint   |
| student_id  | bigint   |
| sorts       | int      |
| create_time | datetime |
| update_time | datetime |

- edu_student_course_teacher (来源 xt_study.student_course_teacher)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(50)  |
| course_name | varchar(255) |
| department  | varchar(255) |
| teacher     | varchar(255) |
| term        | varchar(100) |
| sort        | int          |
| class_type  | varchar(255) |
| room        | varchar(255) |
| day         | int          |
| week        | json         |
| week_desc   | varchar(200) |
| note        | text         |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_course_teacher_time (来源 xt_study.student_course_teacher_time)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| teacher     | varchar(100) |
| department  | varchar(255) |
| school_code | varchar(100) |
| begin_time  | varchar(100) |
| end_time    | varchar(100) |
| term        | varchar(100) |
| sort        | int          |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_course_time (来源 xt_study.student_course_time)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| student_id  | bigint       |
| user_id     | bigint       |
| is_use      | tinyint(1)   |
| name        | varchar(100) |
| begin_time  | varchar(100) |
| end_time    | varchar(100) |
| sort        | int          |
| time_sort   | int          |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_credit_extra (来源 xt_study.student_credit_extra)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| school_code  | varchar(50)  |
| student_id   | bigint       |
| term         | varchar(100) |
| name         | varchar(255) |
| credit       | decimal(10   |
| 4)           |              |
| pass         | tinyint(1)   |
| type         | varchar(100) |
| project_more | text         |
| create_time  | datetime     |
| update_time  | datetime     |

- edu_student_exam_plan (来源 xt_study.student_exam_plan)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| student_id  | bigint       |
| school_code | varchar(50)  |
| name        | varchar(255) |
| exam_time   | varchar(255) |
| local       | varchar(255) |
| seat        | varchar(255) |
| term        | varchar(100) |
| more        | text         |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_grade (来源 xt_study.student_grade)

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| school_code    | varchar(50)  |
| student_id     | bigint       |
| course_name    | varchar(255) |
| course_type    | varchar(150) |
| term           | varchar(150) |
| attributes     | varchar(255) |
| credit         | decimal(10   |
| 4)             |              |
| score          | varchar(100) |
| gpa            | decimal(10   |
| 4)             |              |
| is_pass        | tinyint(1)   |
| pass_methods   | varchar(100) |
| learning_hours | varchar(100) |
| more           | text         |
| detail         | json         |
| create_time    | datetime     |
| update_time    | datetime     |

- edu_student_grade_count (来源 xt_study.student_grade_count)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| school_code  | varchar(50)  |
| student_id   | bigint       |
| term         | varchar(150) |
| gpa          | decimal(10   |
| 4)           |              |
| avg          | decimal(10   |
| 4)           |              |
| anonymous    | int          |
| anonymous_id | bigint       |
| reject_count | tinyint      |
| create_time  | datetime     |
| update_time  | datetime     |

- edu_student_grade_forecast (来源 xt_study.student_grade_forecast)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| school_code | varchar(100) |
| student_id  | bigint       |
| term        | varchar(150) |
| term_avg    | decimal(10   |
| 2)          |              |
| term_gpa    | decimal(10   |
| 2)          |              |
| all_avg     | decimal(10   |
| 2)          |              |
| all_gpa     | decimal(10   |
| 2)          |              |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_grade_forecast_detail (来源 xt_study.student_grade_forecast_detail)

| 字段                | 类型           |
|-------------------|--------------|
| id                | bigint       |
| grade_forecast_id | bigint       |
| course_name       | varchar(255) |
| course_type       | varchar(255) |
| credit            | decimal(10   |
| 2)                |              |
| target_grade      | decimal(10   |
| 2)                |              |

- edu_student_info (来源 xt_study.student_info)

| 字段          | 类型                           |
|-------------|------------------------------|
| id          | bigint                       |
| user_id     | bigint                       |
| school_code | varchar(100)                 |
| campus_code | varchar(100)                 |
| system_type | tinyint(1)                   |
| account     | varchar(100)                 |
| password    | varchar(100)                 |
| department  | varchar(100)                 |
| major       | varchar(100)                 |
| duration    | varchar(100)                 |
| class_type  | varchar(100)                 |
| name        | varchar(100)                 |
| sex         | tinyint(1)                   |
| grade       | varchar(100)                 |
| is_fake     | tinyint(1) unsigned zerofill |
| is_muted    | tinyint(1) unsigned zerofill |
| create_time | datetime                     |
| update_time | datetime                     |

- edu_student_school_start_date (来源 xt_study.student_school_start_date)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| user_id     | bigint       |
| student_id  | bigint       |
| start_date  | datetime     |
| term        | varchar(255) |
| create_time | datetime     |
| update_time | datetime     |

- edu_student_study_credit (来源 xt_study.student_study_credit)

| 字段              | 类型           |
|-----------------|--------------|
| id              | bigint       |
| student_id      | bigint       |
| school_code     | varchar(50)  |
| course_type     | varchar(100) |
| need_credit     | decimal(20   |
| 4)              |              |
| has_credit      | decimal(20   |
| 4)              |              |
| need_credit_set | decimal(20   |
| 4)              |              |
| detail          | json         |
| create_time     | datetime     |
| update_time     | datetime     |

- edu_student_study_plan (来源 xt_study.student_study_plan)

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| school_code    | varchar(100) |
| student_id     | bigint       |
| term           | varchar(150) |
| name           | varchar(150) |
| course_type    | varchar(50)  |
| attribute      | varchar(255) |
| credit         | decimal(10   |
| 5)             |              |
| pass_methods   | varchar(50)  |
| learning_hours | varchar(50)  |
| more           | text         |
| create_time    | datetime     |
| update_time    | datetime     |

- edu_user_plan_complete (来源 xt_study.user_plan_complete)

| 字段            | 类型       |
|---------------|----------|
| id            | bigint   |
| user_id       | bigint   |
| plan_id       | bigint   |
| complete_time | datetime |
| create_time   | datetime |
| update_time   | datetime |

- edu_user_plan_develop (来源 xt_study.user_plan_develop)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| user_id     | bigint       |
| title       | varchar(255) |
| remark      | text         |
| color       | varchar(255) |
| is_repeat   | tinyint      |
| is_all_day  | tinyint      |
| plan_time   | datetime     |
| year        | int          |
| month       | int          |
| day         | int          |
| hour        | int          |
| minute      | int          |
| week        | int          |
| create_time | datetime     |
| update_time | datetime     |

- edu_user_refresh_count (来源 xt_study.user_refresh_count)

| 字段           | 类型       |
|--------------|----------|
| id           | bigint   |
| student_id   | bigint   |
| user_id      | bigint   |
| type         | int      |
| version      | int      |
| refresh_time | datetime |
| create_time  | datetime |
| update_time  | datetime |

- edu_user_push_set (来源 xt_user.user_push_set)

| 字段          | 类型                           |
|-------------|------------------------------|
| id          | bigint                       |
| user_id     | bigint                       |
| plan_push   | tinyint(1) unsigned zerofill |
| course_push | tinyint(1) unsigned zerofill |
| push_num    | int                          |
| version     | bigint                       |
| create_time | datetime                     |
| update_time | datetime                     |

### 社交域（social_*）

- social_circle_common_type (来源 xt_circle.circle_common_type)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| type_name   | varchar(255) |
| type_class  | tinyint(1)   |
| type_number | bigint       |
| icon        | varchar(255) |
| sort        | int          |
| create_time | datetime     |
| update_time | datetime     |

- social_circle_goods (来源 xt_circle.circle_goods)

| 字段             | 类型                           |
|----------------|------------------------------|
| id             | bigint                       |
| user_id        | bigint                       |
| school_code    | varchar(50)                  |
| campus_code    | varchar(50)                  |
| goods_type     | tinyint(1)                   |
| content        | text                         |
| image_url      | json                         |
| type_id        | bigint                       |
| original_price | decimal(10                   |
| 4)             |                              |
| current_price  | decimal(10                   |
| 4)             |                              |
| comment_count  | int                          |
| comment_time   | datetime                     |
| like_count     | int                          |
| view_count     | int                          |
| share_count    | int                          |
| keep_count     | int                          |
| collect_count  | int                          |
| report_count   | int                          |
| is_dicker      | tinyint(1)                   |
| up_status      | tinyint(1)                   |
| up_time        | datetime                     |
| pay_status     | tinyint(1)                   |
| pay_time       | datetime                     |
| check_status   | tinyint(1)                   |
| reject_reason  | text                         |
| is_del         | tinyint(1)                   |
| del_time       | datetime                     |
| is_hide        | tinyint(1) unsigned zerofill |
| publish_time   | datetime                     |
| is_fake        | tinyint(1) unsigned zerofill |
| is_sticky      | tinyint(1) unsigned zerofill |
| is_sticky_type | tinyint(1) unsigned zerofill |
| create_time    | datetime                     |
| update_time    | datetime                     |

- social_circle_goods_addr (来源 xt_circle.circle_goods_addr)

| 字段               | 类型           |
|------------------|--------------|
| id               | bigint       |
| goods_id         | bigint       |
| user_id          | bigint       |
| publish_ip       | varchar(100) |
| ip_province      | varchar(255) |
| publish_addr     | tinytext     |
| publish_province | varchar(255) |
| publish_city     | varchar(255) |
| publish_region   | varchar(255) |
| publish_x        | varchar(255) |
| publish_y        | varchar(255) |
| publish_location | point        |
| select_place     | tinytext     |
| select_x         | varchar(255) |
| select_y         | varchar(255) |
| select_location  | point        |
| create_time      | datetime     |
| update_time      | datetime     |

- social_circle_goods_collect (来源 xt_circle.circle_goods_collect)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| goods_id     | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_goods_comment (来源 xt_circle.circle_goods_comment)

| 字段               | 类型                           |
|------------------|------------------------------|
| id               | bigint                       |
| publisher_id     | bigint                       |
| goods_id         | bigint                       |
| user_id          | bigint                       |
| content          | text                         |
| image_url        | json                         |
| like_count       | int                          |
| report_count     | int                          |
| check_status     | tinyint                      |
| check_reason     | tinytext                     |
| parent_id        | bigint                       |
| reply_user_id    | bigint                       |
| reply_comment_id | bigint                       |
| aite_ids         | json                         |
| is_anonymous     | tinyint(1)                   |
| anonymous_id     | bigint                       |
| is_del           | tinyint(1)                   |
| is_show          | tinyint(1)                   |
| comment_ip       | varchar(100)                 |
| comment_location | varchar(100)                 |
| comment_city     | varchar(255)                 |
| comment_region   | varchar(255)                 |
| is_hide          | tinyint(1) unsigned zerofill |
| create_time      | datetime                     |
| update_time      | datetime                     |

- social_circle_goods_comment_like (来源 xt_circle.circle_goods_comment_like)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| goods_id     | bigint     |
| comment_id   | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_goods_keep (来源 xt_circle.circle_goods_keep)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| goods_id     | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_goods_like (来源 xt_circle.circle_goods_like)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| goods_id     | bigint     |
| publisher_id | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_goods_report (来源 xt_circle.circle_goods_report)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| user_id      | bigint       |
| publisher_id | bigint       |
| goods_id     | bigint       |
| comment_id   | bigint       |
| report_type  | tinyint(1)   |
| reason       | varchar(255) |
| create_time  | datetime     |
| update_time  | datetime     |

- social_circle_goods_view (来源 xt_circle.circle_goods_view)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| goods_id     | bigint     |
| is_del       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_list (来源 xt_circle.circle_list)

| 字段                | 类型                           |
|-------------------|------------------------------|
| id                | bigint                       |
| user_id           | bigint                       |
| publish_campus_id | varchar(255)                 |
| moment_id         | varchar(255)                 |
| school_code       | varchar(50)                  |
| campus_code       | varchar(50)                  |
| type              | tinyint(1)                   |
| join_id           | bigint                       |
| is_public         | tinyint(1)                   |
| is_school         | tinyint(1)                   |
| is_auth           | tinyint(1)                   |
| is_anonymous      | tinyint(1)                   |
| is_comment        | tinyint(1)                   |
| check_status      | tinyint(1)                   |
| reject_reason     | text                         |
| is_del            | tinyint(1)                   |
| del_time          | datetime                     |
| up_status         | tinyint(1)                   |
| publish_time      | datetime                     |
| is_recommend      | tinyint(1)                   |
| is_top_circle     | tinyint(1)                   |
| is_top_school     | tinyint(1)                   |
| is_fake           | tinyint(1)                   |
| is_hide           | tinyint(1) unsigned zerofill |
| create_time       | datetime                     |
| update_time       | datetime                     |

- social_circle_news (来源 xt_circle.circle_news)

| 字段             | 类型                           |
|----------------|------------------------------|
| id             | bigint                       |
| user_id        | bigint                       |
| school_code    | varchar(50)                  |
| campus_code    | varchar(50)                  |
| content        | text                         |
| image_url      | json                         |
| comment_count  | int                          |
| comment_time   | datetime                     |
| like_count     | int                          |
| view_count     | int                          |
| share_count    | int                          |
| keep_count     | int                          |
| collect_count  | int                          |
| report_count   | int                          |
| activity_type  | tinyint(1)                   |
| is_public      | tinyint(1)                   |
| is_school      | tinyint(1)                   |
| is_auth        | tinyint(1)                   |
| is_anonymous   | tinyint(1)                   |
| anonymous_id   | bigint                       |
| is_comment     | tinyint(1)                   |
| check_status   | tinyint(1)                   |
| reject_reason  | text                         |
| is_del         | tinyint(1)                   |
| del_time       | datetime                     |
| aite_ids       | json                         |
| topic_ids      | json                         |
| is_recommend   | tinyint(1)                   |
| is_sticky      | tinyint(1)                   |
| is_sticky_type | tinyint(1)                   |
| is_fake        | tinyint(1)                   |
| hot            | int                          |
| is_push        | tinyint(1) unsigned zerofill |
| is_hide        | tinyint(1) unsigned zerofill |
| publish_time   | datetime                     |
| polish_count   | bigint                       |
| polish_time    | datetime                     |
| create_time    | datetime                     |
| update_time    | datetime                     |

- social_circle_news_addr (来源 xt_circle.circle_news_addr)

| 字段               | 类型           |
|------------------|--------------|
| id               | bigint       |
| news_id          | bigint       |
| user_id          | bigint       |
| publish_ip       | varchar(100) |
| ip_province      | varchar(100) |
| publish_addr     | tinytext     |
| publish_province | varchar(255) |
| publish_city     | varchar(255) |
| publish_region   | varchar(255) |
| publish_x        | varchar(255) |
| publish_y        | varchar(255) |
| publish_location | point        |
| select_place     | tinytext     |
| select_x         | varchar(255) |
| select_y         | varchar(255) |
| select_location  | point        |
| create_time      | datetime     |
| update_time      | datetime     |

- social_circle_news_collect (来源 xt_circle.circle_news_collect)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| news_id      | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_news_comment (来源 xt_circle.circle_news_comment)

| 字段               | 类型                           |
|------------------|------------------------------|
| id               | bigint                       |
| publisher_id     | bigint                       |
| news_id          | bigint                       |
| user_id          | bigint                       |
| content          | text                         |
| image_url        | json                         |
| like_count       | int                          |
| report_count     | int                          |
| check_status     | tinyint                      |
| check_reason     | tinytext                     |
| parent_id        | bigint                       |
| reply_user_id    | bigint                       |
| reply_comment_id | bigint                       |
| aite_ids         | json                         |
| is_del           | tinyint(1)                   |
| is_show          | tinyint(1)                   |
| is_anonymous     | tinyint(1)                   |
| anonymous_id     | bigint                       |
| comment_ip       | varchar(100)                 |
| comment_location | varchar(100)                 |
| comment_city     | varchar(255)                 |
| comment_region   | varchar(255)                 |
| is_hide          | tinyint(1) unsigned zerofill |
| create_time      | datetime                     |
| update_time      | datetime                     |

- social_circle_news_comment_like (来源 xt_circle.circle_news_comment_like)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| news_id      | bigint     |
| comment_id   | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_news_keep (来源 xt_circle.circle_news_keep)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| news_id      | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_news_like (来源 xt_circle.circle_news_like)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| news_id      | bigint     |
| publisher_id | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_news_luck (来源 xt_circle.circle_news_luck)

| 字段             | 类型          |
|----------------|-------------|
| id             | int         |
| user_id        | bigint      |
| news_id        | bigint      |
| luck_type      | tinyint(1)  |
| close_time     | datetime    |
| open_time      | datetime(1) |
| check_status   | tinyint(1)  |
| luck_status    | tinyint(1)  |
| notice_remark  | text        |
| receive_remark | text        |
| update_time    | datetime    |
| create_time    | datetime    |

- social_circle_news_luck_award (来源 xt_circle.circle_news_luck_award)

| 字段          | 类型           |
|-------------|--------------|
| id          | int          |
| user_id     | bigint       |
| news_id     | bigint       |
| luck_id     | bigint       |
| award_title | varchar(255) |
| award_num   | int          |
| award_image | json         |
| sort        | int          |
| update_time | datetime     |
| create_time | datetime     |

- social_circle_news_luck_user (来源 xt_circle.circle_news_luck_user)

| 字段            | 类型       |
|---------------|----------|
| id            | int      |
| user_id       | bigint   |
| news_id       | bigint   |
| luck_id       | bigint   |
| award_id      | bigint   |
| award_user_id | bigint   |
| sort          | int      |
| update_time   | datetime |
| create_time   | datetime |

- social_circle_news_report (来源 xt_circle.circle_news_report)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| user_id      | bigint       |
| publisher_id | bigint       |
| news_id      | bigint       |
| comment_id   | bigint       |
| report_type  | tinyint(1)   |
| reason       | varchar(255) |
| create_time  | datetime     |
| update_time  | datetime     |

- social_circle_news_topic (来源 xt_circle.circle_news_topic)

| 字段            | 类型           |
|---------------|--------------|
| id            | bigint       |
| topic_name    | varchar(255) |
| school_limit  | varchar(50)  |
| school_code   | varchar(50)  |
| user_id       | bigint       |
| view_count    | int          |
| comment_count | int          |
| fake_view     | int          |
| fake_comment  | int          |
| is_hot        | tinyint      |
| hot           | int          |
| is_official   | tinyint      |
| background    | varchar(255) |
| pop_link      | varchar(255) |
| tpc_desc      | varchar(255) |
| create_time   | datetime     |
| update_time   | datetime     |

- social_circle_news_topic_link (来源 xt_circle.circle_news_topic_link)

| 字段          | 类型          |
|-------------|-------------|
| id          | bigint      |
| school_code | varchar(50) |
| topic_id    | bigint      |
| news_id     | bigint      |
| create_time | datetime    |
| update_time | datetime    |

- social_circle_news_view (来源 xt_circle.circle_news_view)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| news_id      | bigint     |
| is_del       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_news_vote (来源 xt_circle.circle_news_vote)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| user_id      | bigint       |
| user_number  | int          |
| news_id      | bigint       |
| vote_title   | varchar(255) |
| close_method | tinyint(1)   |
| vote_status  | tinyint(1)   |
| hand_time    | datetime     |
| close_time   | datetime     |
| check_status | tinyint      |
| create_time  | datetime     |
| update_time  | datetime     |

- social_circle_news_vote_options (来源 xt_circle.circle_news_vote_options)

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| user_id        | bigint       |
| news_id        | bigint       |
| vote_id        | bigint       |
| option_content | varchar(100) |
| option_number  | int          |
| update_time    | datetime     |
| create_time    | datetime     |

- social_circle_news_vote_user (来源 xt_circle.circle_news_vote_user)

| 字段           | 类型       |
|--------------|----------|
| id           | bigint   |
| user_id      | bigint   |
| news_id      | bigint   |
| vote_id      | bigint   |
| option_id    | bigint   |
| vote_user_id | bigint   |
| update_time  | datetime |
| create_time  | datetime |

- social_circle_tissue (来源 xt_circle.circle_tissue)

| 字段                 | 类型                           |
|--------------------|------------------------------|
| id                 | bigint                       |
| school_code        | varchar(50)                  |
| campus_code        | varchar(50)                  |
| user_id            | bigint                       |
| type_id            | bigint                       |
| number             | int                          |
| content            | text                         |
| image_url          | json                         |
| comment_time       | datetime                     |
| comment_count      | int                          |
| like_count         | int                          |
| view_count         | int                          |
| share_count        | int                          |
| keep_count         | int                          |
| collect_count      | int                          |
| report_count       | int                          |
| male_number        | int                          |
| female_number      | int                          |
| join_male_number   | int                          |
| join_female_number | int                          |
| start_time         | datetime                     |
| disband_time       | datetime                     |
| is_gender          | tinyint(1)                   |
| is_group           | tinyint(1)                   |
| group_id           | varchar(100)                 |
| tissue_status      | tinyint(1)                   |
| publish_time       | datetime                     |
| check_status       | tinyint(1)                   |
| reject_reason      | text                         |
| is_del             | tinyint(1)                   |
| del_time           | datetime                     |
| is_hide            | tinyint(1) unsigned zerofill |
| is_fake            | tinyint(1) unsigned zerofill |
| is_sticky          | tinyint(1) unsigned zerofill |
| is_sticky_type     | tinyint(1) unsigned zerofill |
| create_time        | datetime                     |
| update_time        | datetime                     |

- social_circle_tissue_addr (来源 xt_circle.circle_tissue_addr)

| 字段               | 类型           |
|------------------|--------------|
| id               | bigint       |
| tissue_id        | bigint       |
| user_id          | bigint       |
| publish_ip       | varchar(100) |
| ip_province      | varchar(100) |
| publish_addr     | tinytext     |
| publish_province | varchar(255) |
| publish_city     | varchar(255) |
| publish_region   | varchar(255) |
| publish_x        | varchar(255) |
| publish_y        | varchar(255) |
| publish_location | point        |
| select_place     | varchar(255) |
| select_x         | varchar(255) |
| select_y         | varchar(255) |
| select_location  | point        |
| create_time      | datetime     |
| update_time      | datetime     |

- social_circle_tissue_collect (来源 xt_circle.circle_tissue_collect)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| tissue_id    | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_tissue_comment (来源 xt_circle.circle_tissue_comment)

| 字段               | 类型                           |
|------------------|------------------------------|
| id               | bigint                       |
| publisher_id     | bigint                       |
| tissue_id        | bigint                       |
| user_id          | bigint                       |
| content          | text                         |
| image_url        | json                         |
| like_count       | int                          |
| report_count     | int                          |
| check_status     | tinyint                      |
| check_reason     | tinytext                     |
| parent_id        | bigint                       |
| reply_user_id    | bigint                       |
| reply_comment_id | bigint                       |
| aite_ids         | json                         |
| is_anonymous     | tinyint(1)                   |
| anonymous_id     | bigint                       |
| is_del           | tinyint(1)                   |
| is_show          | tinyint(1)                   |
| comment_ip       | varchar(100)                 |
| comment_location | varchar(100)                 |
| comment_city     | varchar(255)                 |
| comment_region   | varchar(255)                 |
| is_hide          | tinyint(1) unsigned zerofill |
| create_time      | datetime                     |
| update_time      | datetime                     |

- social_circle_tissue_comment_like (来源 xt_circle.circle_tissue_comment_like)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| tissue_id    | bigint     |
| comment_id   | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_tissue_join (来源 xt_circle.circle_tissue_join)

| 字段          | 类型          |
|-------------|-------------|
| id          | bigint      |
| user_id     | bigint      |
| sex         | int         |
| tissue_id   | bigint      |
| school_code | varchar(50) |
| campus_code | varchar(50) |
| status      | tinyint(1)  |
| join_time   | datetime    |
| out_time    | datetime    |
| create_time | datetime    |
| update_time | datetime    |

- social_circle_tissue_keep (来源 xt_circle.circle_tissue_keep)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| tissue_id    | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_tissue_like (来源 xt_circle.circle_tissue_like)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| tissue_id    | bigint     |
| publisher_id | bigint     |
| status       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_tissue_report (来源 xt_circle.circle_tissue_report)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| user_id      | bigint       |
| publisher_id | bigint       |
| tissue_id    | bigint       |
| comment_id   | bigint       |
| report_type  | tinyint(1)   |
| reason       | varchar(255) |
| create_time  | datetime     |
| update_time  | datetime     |

- social_circle_tissue_view (来源 xt_circle.circle_tissue_view)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| user_id      | bigint     |
| publisher_id | bigint     |
| tissue_id    | bigint     |
| is_del       | tinyint(1) |
| create_time  | datetime   |
| update_time  | datetime   |

- social_circle_top_config (来源 xt_circle.circle_top_config)

| 字段                     | 类型          |
|------------------------|-------------|
| id                     | bigint      |
| school_code            | varchar(50) |
| one_hour_price         | decimal(10  |
| 2)                     |             |
| two_hour_price         | decimal(10  |
| 2)                     |             |
| four_hour_price        | decimal(10  |
| 2)                     |             |
| twelve_hour_price      | decimal(10  |
| 2)                     |             |
| twenty_four_hour_price | decimal(10  |
| 2)                     |             |
| create_time            | datetime    |
| update_time            | datetime    |

- social_circle_top_pay (来源 xt_circle.circle_top_pay)

| 字段              | 类型           |
|-----------------|--------------|
| id              | bigint       |
| user_id         | bigint       |
| school_code     | varchar(50)  |
| top_type        | tinyint      |
| news_id         | bigint       |
| order_num       | varchar(255) |
| amount          | decimal(10   |
| 2)              |              |
| order_status    | tinyint      |
| pay_status      | tinyint      |
| cancel_state    | tinyint      |
| pay_time_out    | int          |
| pay_trade_no    | varchar(255) |
| pay_time        | datetime     |
| refund_trade_no | varchar(255) |
| refund_time     | datetime     |
| check_status    | tinyint      |
| is_delete       | tinyint      |
| start_time      | datetime     |
| end_time        | datetime     |
| create_time     | datetime     |
| update_time     | datetime     |

- social_message_comment (来源 xt_message.message_comment)

| 字段             | 类型         |
|----------------|------------|
| id             | bigint     |
| publisher_id   | bigint     |
| send_id        | bigint     |
| to_id          | bigint     |
| message_type   | tinyint(1) |
| circle_type    | tinyint(1) |
| circle_id      | bigint     |
| circle_content | text       |
| circle_img     | text       |
| comment_img    | text       |
| comment_id     | bigint     |
| title          | text       |
| content        | text       |
| is_anonymous   | tinyint(1) |
| anonymous_id   | bigint     |
| is_read        | tinyint(1) |
| creat_time     | datetime   |
| update_time    | datetime   |

- social_message_like_collect (来源 xt_message.message_like_collect)

| 字段           | 类型         |
|--------------|------------|
| id           | bigint     |
| publisher_id | bigint     |
| send_id      | bigint     |
| to_id        | bigint     |
| circle_id    | bigint     |
| message_type | tinyint(1) |
| circle_type  | tinyint(1) |
| title        | text       |
| img_url      | text       |
| is_read      | tinyint(1) |
| creat_time   | datetime   |
| update_time  | datetime   |

- social_message_notice (来源 xt_message.message_notice)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| user_id      | bigint       |
| object_name  | varchar(100) |
| msg_title    | varchar(100) |
| msg_content  | varchar(255) |
| msg_type     | varchar(100) |
| im_object    | varchar(255) |
| redirect_url | varchar(255) |
| is_read      | tinyint(1)   |
| create_time  | datetime     |
| update_time  | datetime     |

- social_msg_active (来源 xt_message.msg_active)

| 字段            | 类型           |
|---------------|--------------|
| id            | bigint       |
| school_code   | varchar(100) |
| push_identity | varchar(100) |
| object_name   | varchar(100) |
| msg_title     | varchar(100) |
| msg_content   | varchar(255) |
| msg_type      | varchar(100) |
| im_object     | varchar(255) |
| push_type     | varchar(100) |
| redirect_url  | varchar(255) |
| is_pushed     | tinyint(1)   |
| end_time      | datetime     |
| create_time   | datetime     |
| update_time   | datetime     |

- social_msg_active_push (来源 xt_message.msg_active_push)

| 字段          | 类型         |
|-------------|------------|
| id          | bigint     |
| msg_id      | bigint     |
| user_id     | bigint     |
| is_read     | tinyint(1) |
| create_time | datetime   |
| update_time | datetime   |

- social_takeout_his_addr (来源 xt_takeout.his_addr)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| school_code  | varchar(100) |
| campus_code  | varchar(100) |
| user_id      | bigint       |
| send_addr    | varchar(255) |
| receive_addr | varchar(255) |
| is_del       | tinyint(1)   |
| create_time  | datetime     |
| update_tiem  | datetime     |

- social_takeout_his_order_runner (来源 xt_takeout.his_order_runner)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| order_num   | varchar(255) |
| take_status | tinyint(1)   |
| runner_id   | bigint       |
| user_id     | int          |
| create_time | datetime     |
| update_time | datetime     |

- social_takeout_runner_config (来源 xt_takeout.runner_config)

| 字段           | 类型           |
|--------------|--------------|
| id           | bigint       |
| school_code  | varchar(255) |
| runner_ratio | decimal(10   |
| 2)           |              |
| agent_ratio  | decimal(10   |
| 2)           |              |
| create_time  | datetime     |
| update_time  | datetime     |

- social_takeout_runner_orders (来源 xt_takeout.runner_orders)

| 字段              | 类型           |
|-----------------|--------------|
| id              | bigint       |
| school_code     | varchar(100) |
| campus_code     | varchar(100) |
| user_id         | bigint       |
| runner_id       | bigint       |
| order_no        | varchar(255) |
| out_refund_no   | varchar(255) |
| order_type      | tinyint(1)   |
| send_addr       | varchar(255) |
| receive_addr    | varchar(255) |
| send_name       | varchar(255) |
| send_phone      | varchar(255) |
| receiver_name   | varchar(255) |
| receiver_phone  | varchar(100) |
| hide_info       | varchar(255) |
| runner_phone    | varchar(255) |
| order_title     | varchar(255) |
| order_detail    | varchar(255) |
| detail_url      | json         |
| hope_time       | datetime     |
| price           | decimal(10   |
| 2)              |              |
| refund_price    | decimal(10   |
| 2)              |              |
| ratio           | decimal(10   |
| 2)              |              |
| agent_price     | decimal(10   |
| 2)              |              |
| runner_price    | decimal(10   |
| 2)              |              |
| wait_time       | datetime     |
| sex_limit       | tinyint(1)   |
| face_status     | tinyint(1)   |
| pay_type        | tinyint(1)   |
| pay_time        | datetime     |
| pay_status      | tinyint(1)   |
| wx_pay_num      | varchar(255) |
| refund_trade_no | varchar(255) |
| refund_time     | datetime     |
| order_status    | tinyint(1)   |
| cancel_status   | tinyint(1)   |
| create_time     | datetime     |
| update_time     | datetime     |

- social_user_anonymous (来源 xt_user.user_anonymous)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| nick_name   | varchar(50)  |
| avatar      | varchar(100) |
| create_time | datetime     |
| update_time | datetime     |

- social_user_card (来源 xt_user.user_card)

| 字段          | 类型         |
|-------------|------------|
| id          | bigint     |
| user_id     | bigint     |
| card_type   | tinyint(1) |
| card_num    | int        |
| version     | int        |
| create_time | datetime   |
| update_time | datetime   |

- social_user_check (来源 xt_user.user_check)

| 字段          | 类型       |
|-------------|----------|
| id          | bigint   |
| user_id     | bigint   |
| check_year  | int      |
| check_month | int      |
| check_day   | int      |
| is_sign     | tinyint  |
| coin_num    | int      |
| create_time | datetime |
| update_time | datetime |

- social_user_check_config (来源 xt_user.user_check_config)

| 字段             | 类型           |
|----------------|--------------|
| id             | bigint       |
| school_code    | varchar(100) |
| check_coin_num | int          |
| check_type     | tinyint(1)   |
| type_day       | int          |
| is_del         | tinyint(1)   |
| create_time    | datetime     |
| update_time    | datetime     |

- social_user_coin_fund (来源 xt_user.user_coin_fund)

| 字段          | 类型       |
|-------------|----------|
| id          | bigint   |
| user_id     | bigint   |
| coin_num    | int      |
| create_time | datetime |
| update_time | datetime |

- social_user_coin_fund_detail (来源 xt_user.user_coin_fund_detail)

| 字段          | 类型         |
|-------------|------------|
| id          | bigint     |
| user_id     | bigint     |
| coin_num    | int        |
| opt_type    | tinyint(1) |
| coin_source | tinyint(1) |
| create_time | datetime   |
| update_time | datetime   |

- social_user_follow_friend (来源 xt_user.user_follow_friend)

| 字段             | 类型         |
|----------------|------------|
| id             | bigint     |
| user_id        | bigint     |
| follow_user_id | bigint     |
| is_read        | bigint     |
| status         | tinyint(1) |
| create_time    | datetime   |
| update_time    | datetime   |

- social_user_fund (来源 xt_user.user_fund)

| 字段          | 类型         |
|-------------|------------|
| id          | bigint     |
| user_id     | bigint     |
| balance     | decimal(10 |
| 2)          |            |
| version     | bigint     |
| create_time | datetime   |
| update_time | datetime   |

- social_user_fund_detail (来源 xt_user.user_fund_detail)

| 字段                | 类型           |
|-------------------|--------------|
| id                | bigint       |
| user_id           | bigint       |
| balance           | decimal(20   |
| 2)                |              |
| order_no          | varchar(255) |
| order_desc        | varchar(255) |
| type              | tinyint      |
| method            | varchar(10)  |
| state             | tinyint      |
| fee_freeze_time   | int          |
| order_create_time | datetime     |
| create_time       | datetime     |
| update_time       | datetime     |

- social_user_fund_extract (来源 xt_user.user_fund_extract)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| user_id     | bigint       |
| balance     | decimal(20   |
| 2)          |              |
| order_num   | varchar(255) |
| trade_no    | varchar(255) |
| state       | tinyint      |
| fail_reason | text         |
| create_time | datetime     |
| update_time | datetime     |

- social_user_jump_page (来源 xt_user.user_jump_page)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| user_id     | bigint       |
| page_path   | varchar(255) |
| create_time | datetime     |
| update_time | datetime     |

- social_user_location (来源 xt_user.user_location)

| 字段          | 类型           |
|-------------|--------------|
| id          | bigint       |
| user_id     | bigint       |
| ip          | varchar(100) |
| location_x  | varchar(100) |
| location_y  | varchar(100) |
| location    | point        |
| province    | varchar(100) |
| city        | varchar(100) |
| district    | varchar(100) |
| street      | varchar(255) |
| address     | varchar(255) |
| create_time | datetime     |
| update_time | datetime     |

- social_user_push_msg (来源 xt_user.user_push_msg)

| 字段          | 类型       |
|-------------|----------|
| id          | bigint   |
| push_msg    | text     |
| push_time   | datetime |
| create_time | datetime |
| update_time | datetime |

- social_user_push_msg_status (来源 xt_user.user_push_msg_status)

| 字段          | 类型         |
|-------------|------------|
| id          | bigint     |
| push_msg_id | bigint     |
| user_id     | bigint     |
| push_status | tinyint(1) |
| create_time | datetime   |
| update_time | datetime   |

- social_user_push_set (来源 xt_user.user_push_set)

| 字段          | 类型                           |
|-------------|------------------------------|
| id          | bigint                       |
| user_id     | bigint                       |
| plan_push   | tinyint(1) unsigned zerofill |
| course_push | tinyint(1) unsigned zerofill |
| push_num    | int                          |
| version     | bigint                       |
| create_time | datetime                     |
| update_time | datetime                     |

### 暂缓或不在范围内（管理端或二手书/订单）

- xt_system.*（管理端 RBAC）-> 后续拆为 edu_admin_* 与 social_admin_*。
- xt_user.agent_* -> 管理端相关，延后到管理端域。
- xt_order.* 与 xt-book 模块 -> 暂缓。

## IM 处理（当前与后续）

- 现阶段移除融云 SDK 相关代码。
- 以接口预留 IM 能力，提供 Noop 实现。
- core_user.im_id 与 core_user.im_token 保持可空，便于未来接入 IM。

## 实施步骤（建议）

1. 新建仓库结构与 common 模块，统一依赖与构建配置（JDK 25 + Spring Boot 4）。
2. 新建数据库结构，创建 core_/edu_/social_ 表（字段如上），并为所有 core_* 表补充 edu_enabled/social_enabled。
3. 迁移教务模块：将 xt-study 迁入 edu-app，调整包名与表前缀；必要处新增 edu_user_push_set。
4. 迁移社交模块：将 xt-circle、xt-message、xt-takeout 与 xt-user 的社交部分迁入 social-app，调整表前缀，拆分
   social_user_push_set，并移除 IM 逻辑。
5. 迁移 core 用户/学校/平台配置：将 user、school 与 platform 配置迁入 common + core 层，并更新 edu/social 引用。
6. 管理端应用：暂缓或先建空壳 edu-admin 与 social-admin；实现时保留 xt_user_id 映射。
7. 移除 gateway/feign/nacos 依赖，未来确有需要再引入。

## 已确认决策

1. platform_* 表迁入 core_*，并包含 edu_enabled/social_enabled。
2. user_push_set 拆分为 edu_user_push_set 与 social_user_push_set。
3. user_check 与 user_check_config 归属 social_*。
