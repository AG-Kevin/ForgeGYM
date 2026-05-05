DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS course_schedule;
DROP TABLE IF EXISTS store;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS coach;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS sys_user;

-- 用户表：系统登录账户
CREATE TABLE sys_user (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                          username VARCHAR(64) NOT NULL UNIQUE COMMENT '登录用户名',
                          password_hash VARCHAR(128) NOT NULL COMMENT '密码哈希值',
                          role VARCHAR(16) NOT NULL COMMENT '角色：ADMIN-管理员、COACH-教练、MEMBER-会员',
                          ref_id BIGINT NULL COMMENT '关联ID：对应coach.id或member.id'
) COMMENT='系统用户表';

-- 教练表
CREATE TABLE coach (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                       name VARCHAR(64) NOT NULL COMMENT '教练姓名',
                       phone VARCHAR(32) COMMENT '联系电话',
                       intro VARCHAR(255) COMMENT '个人简介',
                       tags VARCHAR(255) COMMENT '标签：瑜伽、普拉提等，逗号分隔',
                       avatar VARCHAR(255) COMMENT '头像地址'
) COMMENT='教练信息表';

-- 会员表
CREATE TABLE member (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                        name VARCHAR(64) NOT NULL COMMENT '会员姓名',
                        phone VARCHAR(32) COMMENT '联系电话',
                        expire_date DATE COMMENT '会员有效期截止日期',
                        balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额'
) COMMENT='会员信息表';

-- 门店表
CREATE TABLE store (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                       name VARCHAR(64) NOT NULL COMMENT '门店名称',
                       region VARCHAR(32) COMMENT '所在区域',
                       address VARCHAR(255) COMMENT '详细地址',
                       business_hours VARCHAR(64) COMMENT '营业时间',
                       cover_image VARCHAR(255) COMMENT '封面图片',
                       contact_phone VARCHAR(32) COMMENT '联系电话',
                       capacity INT NOT NULL DEFAULT 0 COMMENT '最大容纳人数',
                       status VARCHAR(16) NOT NULL DEFAULT 'OPEN' COMMENT '状态：OPEN-营业中、CLOSED-关闭'
) COMMENT='门店信息表';

-- 课程表
CREATE TABLE course (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                        name VARCHAR(64) NOT NULL COMMENT '课程名称',
                        description VARCHAR(255) COMMENT '课程描述',
                        duration_minutes INT COMMENT '课程时长（分钟）',
                        type VARCHAR(16) NOT NULL DEFAULT 'GROUP' COMMENT '课程类型：GROUP-团课、PRIVATE-私教',
                        price DECIMAL(10, 2) NOT NULL DEFAULT 50.00 COMMENT '课程价格',
                        category VARCHAR(32) COMMENT '课程分类：瑜伽、舞蹈、力量等',
                        level VARCHAR(32) COMMENT '难度等级：初级、中级、高级',
                        calories INT COMMENT '消耗卡路里',
                        cover_image VARCHAR(255) COMMENT '课程封面图',
                        video_url VARCHAR(255) COMMENT '课程视频链接',
                        status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用、INACTIVE-禁用',
                        summary VARCHAR(255) COMMENT '课程摘要'
) COMMENT='课程信息表';

-- 课程排期表
CREATE TABLE course_schedule (
                                 id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                                 course_id BIGINT NOT NULL COMMENT '课程ID',
                                 coach_id BIGINT NOT NULL COMMENT '授课教练ID',
                                 store_id BIGINT NOT NULL COMMENT '上课门店ID',
                                 start_time DATETIME NOT NULL COMMENT '开始时间',
                                 end_time DATETIME NOT NULL COMMENT '结束时间',
                                 capacity INT NOT NULL COMMENT '最大可预约人数'
) COMMENT='课程排期表';

-- 预约表
CREATE TABLE booking (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                         schedule_id BIGINT NOT NULL COMMENT '排期ID',
                         member_id BIGINT NOT NULL COMMENT '会员ID',
                         status VARCHAR(16) NOT NULL COMMENT '预约状态：SUCCESS-预约成功、CANCELLED-已取消',
                         created_at DATETIME NOT NULL COMMENT '预约时间',
                         amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
                         attendance_status VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED' COMMENT '出勤状态：SCHEDULED-待上课、ATTENDED-已出勤、ABSENT-缺勤',
                         rating INT COMMENT '评分：1-5星',
                         review_content VARCHAR(255) COMMENT '评价内容',
                         reviewed_at DATETIME COMMENT '评价时间',
                         UNIQUE KEY uk_booking (schedule_id, member_id)
) COMMENT='课程预约表';
