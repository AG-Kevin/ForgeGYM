DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS course_schedule;
DROP TABLE IF EXISTS store;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS coach;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    role VARCHAR(16) NOT NULL,
    ref_id BIGINT NULL
);

CREATE TABLE coach (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32),
    intro VARCHAR(255),
    tags VARCHAR(255),
    avatar VARCHAR(255)
);

CREATE TABLE member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32),
    expire_date DATE,
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE store (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    region VARCHAR(32),
    address VARCHAR(255),
    business_hours VARCHAR(64),
    cover_image VARCHAR(255),
    contact_phone VARCHAR(32),
    capacity INT NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL DEFAULT 'OPEN'
);

CREATE TABLE course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(255),
    duration_minutes INT,
    type VARCHAR(16) NOT NULL DEFAULT 'GROUP',
    price DECIMAL(10, 2) NOT NULL DEFAULT 50.00,
    category VARCHAR(32),
    level VARCHAR(32),
    calories INT,
    cover_image VARCHAR(255),
    video_url VARCHAR(255),
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    summary VARCHAR(255)
);

CREATE TABLE course_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    coach_id BIGINT NOT NULL,
    store_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    capacity INT NOT NULL
);

CREATE TABLE booking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schedule_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL,
    amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    attendance_status VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED',
    rating INT,
    review_content VARCHAR(255),
    reviewed_at DATETIME,
    UNIQUE KEY uk_booking (schedule_id, member_id)
);
