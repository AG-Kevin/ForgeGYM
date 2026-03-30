INSERT IGNORE INTO coach (id, name, phone, intro, tags, avatar)
VALUES 
(1, '教练A', '13800000001', '擅长力量与体态训练，帮助学员建立长期可持续的训练习惯。', '力量训练,体态改善', '/assets/images/avatars/coach-256x256.svg'),
(2, '教练B', '13800000002', '注重呼吸与动作控制，让训练更安全、更高效。', '瑜伽普拉提,柔韧提升', '/assets/images/avatars/coach-256x256.svg'),
(3, '教练C', '13800000003', '节奏与爆发结合，让训练过程更有趣也更有挑战。', '拳击舞蹈,燃脂', '/assets/images/avatars/coach-256x256.svg'),
(4, '教练D', '13800000004', '专注于核心力量强化和产后恢复训练。', '核心训练,产后恢复', '/assets/images/avatars/coach-256x256.svg'),
(5, '教练E', '13800000005', '高强度间歇训练专家，快速提升心肺功能。', 'HIIT,减脂塑形', '/assets/images/avatars/coach-256x256.svg');

INSERT IGNORE INTO member (id, name, phone, expire_date, balance)
VALUES 
(1, '会员A', '13900000001', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 800.00),
(2, '会员B', '13900000002', DATE_ADD(CURDATE(), INTERVAL 60 DAY), 600.00),
(3, '会员C', '13900000003', DATE_ADD(CURDATE(), INTERVAL 90 DAY), 500.00),
(4, '会员D', '13900000004', DATE_ADD(CURDATE(), INTERVAL 15 DAY), 300.00),
(5, '会员E', '13900000005', DATE_ADD(CURDATE(), INTERVAL 120 DAY), 900.00);

INSERT IGNORE INTO store (id, name, region, address, business_hours, cover_image, contact_phone, capacity, status)
VALUES 
(1, '城市健身舱·中心店', '东区', '示例路 100 号', '07:00-23:00', '/assets/images/stores/store1.jpg', '021-10000001', 120, 'OPEN'),
(2, '城市健身舱·公园店', '西区', '示例路 200 号', '07:00-23:00', '/assets/images/stores/store2.jpg', '021-10000002', 90, 'OPEN'),
(3, '城市健身舱·科技店', '南区', '示例路 300 号', '07:00-23:00', '/assets/images/stores/store3.jpg', '021-10000003', 150, 'OPEN');

INSERT IGNORE INTO course (id, name, description, duration_minutes, type, price, category, level, calories, cover_image, video_url, status, summary)
VALUES 
(1, '燃脂团课', '适合新手的燃脂课程，高效消耗卡路里', 60, 'GROUP', 50.00, '燃脂塑形', '入门', 420, '/assets/images/course-slim.jpg', 'https://www.w3schools.com/html/mov_bbb.mp4', 'ACTIVE', '高效燃脂与体能提升'),
(2, '瑜伽普拉提', '柔韧与核心控制，改善体态', 60, 'GROUP', 50.00, '身心平衡', '入门-进阶', 260, '/assets/images/course-yoga.jpg', 'https://www.w3schools.com/html/movie.mp4', 'ACTIVE', '核心稳定与体态修复'),
(3, '1v1 私人教练', '专属定制化训练，1对1指导', 60, '1V1', 200.00, '私教训练', '进阶', 380, '/assets/images/course-pt.jpg', 'https://www.w3schools.com/html/mov_bbb.mp4', 'ACTIVE', '定制训练与动作纠正'),
(4, '拳击舞蹈', '爆发与节奏，释放压力', 50, 'GROUP', 50.00, '搏击有氧', '进阶', 450, '/assets/images/course-box.jpg', 'https://www.w3schools.com/html/movie.mp4', 'ACTIVE', '节奏训练与爆发提升'),
(5, '儿童潜能', '面向家庭与儿童的体能课程', 60, 'GROUP', 50.00, '亲子成长', '入门', 220, '/assets/images/course-kids.jpg', 'https://www.w3schools.com/html/mov_bbb.mp4', 'ACTIVE', '体能启蒙与协作训练');

INSERT IGNORE INTO course_schedule (id, course_id, coach_id, store_id, start_time, end_time, capacity)
VALUES 
(1, 1, 1, 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 60 MINUTE, 10),
(2, 2, 2, 2, DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 2 HOUR, DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 3 HOUR, 15),
(3, 3, 5, 1, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY) + INTERVAL 45 MINUTE, 12),
(4, 4, 3, 3, DATE_ADD(NOW(), INTERVAL 2 DAY) + INTERVAL 3 HOUR, DATE_ADD(NOW(), INTERVAL 2 DAY) + INTERVAL 4 HOUR, 20),
(5, 5, 4, 2, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 60 MINUTE, 8);

INSERT IGNORE INTO sys_user (id, username, password_hash, role, ref_id)
VALUES
    (1, 'admin', 'bf6b5bdb74c79ece9fc0ad0ac9fb0359f9555d4f35a83b2e6ec69ae99e09603d', 'ADMIN', NULL),
    (2, 'coach1', '6b2569fba6f7d2bc463c664350e3ab9a12badd08d8a1e822e7e8cc56d28a79c2', 'COACH', 1),
    (3, 'member1', '2c3dac1086c1d0481c1b77f3778e3e2b8d16b28f769df518c08000bc56cc6d12', 'MEMBER', 1),
    (4, 'coach2', '6b2569fba6f7d2bc463c664350e3ab9a12badd08d8a1e822e7e8cc56d28a79c2', 'COACH', 2),
    (5, 'member2', '2c3dac1086c1d0481c1b77f3778e3e2b8d16b28f769df518c08000bc56cc6d12', 'MEMBER', 2);

INSERT IGNORE INTO booking (id, schedule_id, member_id, status, created_at, amount, attendance_status, rating, review_content, reviewed_at)
VALUES
    (1, 1, 1, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY), 50.00, 'ATTENDED', 5, '课程节奏很好，教练指导很细致。', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
    (2, 2, 2, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY), 50.00, 'ATTENDED', 4, '环境不错，动作讲解清晰。', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (3, 3, 1, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 6 HOUR), 200.00, 'SCHEDULED', NULL, NULL, NULL);
