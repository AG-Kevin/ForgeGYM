INSERT INTO coach (id, name, phone, intro) VALUES (1, '教练A', '13800000000', '擅长力量训练与体态改善');
INSERT INTO member (id, name, phone, expire_date, balance) VALUES (1, '会员A', '13900000000', DATE '2099-12-31', 500.00);
INSERT INTO store (id, name, region, address, business_hours, cover_image, contact_phone, capacity, status)
VALUES (1, '城市健身舱·中心店', '东区', '示例路 100 号', '07:00-23:00', '/assets/images/stores/store1.jpg', '021-10000001', 120, 'OPEN');
INSERT INTO course (id, name, description, duration_minutes, type, price, category, level, calories, cover_image, video_url, status, summary)
VALUES (1, '燃脂团课', '适合新手的燃脂课程', 60, 'GROUP', 50.00, '燃脂塑形', '入门', 420, '/assets/images/course-slim.jpg', 'https://www.w3schools.com/html/mov_bbb.mp4', 'ACTIVE', '高效燃脂与体能提升');
INSERT INTO course_schedule (id, course_id, coach_id, store_id, start_time, end_time, capacity)
VALUES (1, 1, 1, 1, TIMESTAMP '2099-01-01 10:00:00', TIMESTAMP '2099-01-01 11:00:00', 10);

INSERT INTO sys_user (id, username, password_hash, role, ref_id) VALUES
    (1, 'admin', 'bf6b5bdb74c79ece9fc0ad0ac9fb0359f9555d4f35a83b2e6ec69ae99e09603d', 'ADMIN', NULL),
    (2, 'coach1', '667586fab17b3ffe6a1bcd798892c7af34e50bb63f2f648c2fea330f3dee76aa', 'COACH', 1),
    (3, 'member1', '2c3dac1086c1d0481c1b77f3778e3e2b8d16b28f769df518c08000bc56cc6d12', 'MEMBER', 1);

INSERT INTO booking (id, schedule_id, member_id, status, created_at, amount, attendance_status, rating, review_content, reviewed_at)
VALUES (1, 1, 1, 'COMPLETED', TIMESTAMP '2098-12-31 09:00:00', 50.00, 'ATTENDED', 5, '本地演示评价', TIMESTAMP '2098-12-31 12:00:00');
