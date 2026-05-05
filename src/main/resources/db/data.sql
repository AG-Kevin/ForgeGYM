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
(1, 1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 10 HOUR, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 12 HOUR, 10),
(2, 2, 2, 2, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 14 HOUR, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 16 HOUR, 15),
(3, 3, 5, 1, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 19 HOUR, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 21 HOUR, 12),
(4, 4, 3, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 14 HOUR, DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 16 HOUR, 20),
(5, 5, 4, 2, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 8 HOUR, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 10 HOUR, 8);

INSERT IGNORE INTO sys_user (id, username, password_hash, role, ref_id)
VALUES
    (1, 'admin', 'bf6b5bdb74c79ece9fc0ad0ac9fb0359f9555d4f35a83b2e6ec69ae99e09603d', 'ADMIN', NULL),
    (2, 'coach1', '667586fab17b3ffe6a1bcd798892c7af34e50bb63f2f648c2fea330f3dee76aa', 'COACH', 1),
    (3, 'member1', '2c3dac1086c1d0481c1b77f3778e3e2b8d16b28f769df518c08000bc56cc6d12', 'MEMBER', 1),
    (4, 'coach2', '7e3009e9a0addecadf34320fe6cf0c8676bc58572492ca61c0dd168a42647e8a', 'COACH', 2),
    (5, 'member2', '54c6b3532964d57e6c6d8a8d7b0c1bdc6a436d2aac176c8d7d2c98cada41610c', 'MEMBER', 2);

INSERT IGNORE INTO booking (id, schedule_id, member_id, status, created_at, amount, attendance_status, rating, review_content, reviewed_at)
VALUES
    (1, 1, 1, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY), 50.00, 'ATTENDED', 5, '课程节奏很好，教练指导很细致。', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
    (2, 2, 2, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY), 50.00, 'ATTENDED', 4, '环境不错，动作讲解清晰。', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (3, 3, 1, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 4 DAY), 200.00, 'ATTENDED', 5, '私教课非常棒！', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 增加更多教练 (ID 6-8)
INSERT IGNORE INTO coach (id, name, phone, intro, tags, avatar)
VALUES 
(6, '教练F', '13800000006', '资深皮拉提斯大器械教练，专注体态矫正。', '皮拉提斯,体态矫正', '/assets/images/avatars/coach-256x256.svg'),
(7, '教练G', '13800000007', '职业健美运动员，擅长增肌与赛前指导。', '增肌,力量训练', '/assets/images/avatars/coach-256x256.svg'),
(8, '教练H', '13800000008', '尊巴（Zumba）官方认证教练，快乐燃脂专家。', '尊巴,舞蹈', '/assets/images/avatars/coach-256x256.svg');

-- 增加更多会员 (ID 6-10)
INSERT IGNORE INTO member (id, name, phone, expire_date, balance)
VALUES 
(6, '会员F', '13900000006', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1200.00), -- 即将到期
(7, '会员G', '13900000007', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 50.00),   -- 已过期
(8, '会员H', '13900000008', DATE_ADD(CURDATE(), INTERVAL 365 DAY), 0.00),   -- 余额为0
(9, '会员I', '13900000009', DATE_ADD(CURDATE(), INTERVAL 180 DAY), 5000.00), -- 高余额
(10, '会员J', '13900000010', DATE_ADD(CURDATE(), INTERVAL 90 DAY), 100.00);

-- 增加更多门店 (ID 4-5)
INSERT IGNORE INTO store (id, name, region, address, business_hours, cover_image, contact_phone, capacity, status)
VALUES 
(4, '城市健身舱·北区店', '北区', '示例路 400 号', '08:00-22:00', '/assets/images/stores/store1.jpg', '021-10000004', 80, 'OPEN'),
(5, '城市健身舱·旗舰店(维护中)', '中区', '示例路 500 号', '09:00-18:00', '/assets/images/stores/store2.jpg', '021-10000005', 200, 'CLOSED');

-- 增加更多课程 (ID 6-9)
INSERT IGNORE INTO course (id, name, description, duration_minutes, type, price, category, level, calories, cover_image, video_url, status, summary)
VALUES 
(6, '增肌进阶', '针对有基础的学员，突破力量瓶颈', 90, 'GROUP', 80.00, '力量训练', '进阶', 600, '/assets/images/course-pt.jpg', 'https://www.w3schools.com/html/mov_bbb.mp4', 'ACTIVE', '力量突破与维度提升'),
(7, '皮拉提斯大器械', '1对1器械指导，深度拉伸与核心激活', 60, '1V1', 250.00, '身心平衡', '入门-进阶', 320, '/assets/images/course-yoga.jpg', 'https://www.w3schools.com/html/movie.mp4', 'ACTIVE', '器械辅助与核心重塑'),
(8, '舞蹈', '动感节奏，释放压力，快乐燃脂', 50, 'GROUP', 40.00, '燃脂塑形', '入门', 400, '/assets/images/course-box.jpg', 'https://www.w3schools.com/html/movie.mp4', 'ACTIVE', '舞蹈律动与热量消耗'),
(9, 'HIIT极限挑战', '高强度间歇训练，挑战体能极限', 45, 'GROUP', 60.00, '燃脂塑形', '挑战', 550, '/assets/images/course-slim.jpg', 'https://www.w3schools.com/html/mov_bbb.mp4', 'ACTIVE', '高强间歇与代谢提升');

-- 增加更多排课 (ID 6-15)
INSERT IGNORE INTO course_schedule (id, course_id, coach_id, store_id, start_time, end_time, capacity)
VALUES 
(6, 6, 7, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 19 HOUR, DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 21 HOUR, 15),
(7, 7, 6, 4, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 10 HOUR, DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 12 HOUR, 1), -- 1对1 只有1个名额
(8, 8, 8, 4, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 19 HOUR, DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 21 HOUR, 30),
(9, 9, 5, 3, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 16 HOUR, DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 18 HOUR, 20),
(10, 1, 1, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 10 HOUR, DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 12 HOUR, 10),
(11, 2, 2, 2, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 14 HOUR, DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 16 HOUR, 15),
(12, 3, 5, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 16 HOUR, DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL 18 HOUR, 1),
(13, 6, 7, 4, DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 19 HOUR, DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL 21 HOUR, 12),
(14, 8, 8, 1, DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 10 HOUR, DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL 12 HOUR, 20),
(15, 7, 6, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 16 HOUR, DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL 18 HOUR, 2); -- 满员测试

-- 增加更多用户 (ID 6-13)
-- 密码 hash 为 123456 (6b2569fba6f7d2bc463c664350e3ab9a12badd08d8a1e822e7e8cc56d28a79c2) 
-- 或使用 member123 (2c3dac1086c1d0481c1b77f3778e3e2b8d16b28f769df518c08000bc56cc6d12)
INSERT IGNORE INTO sys_user (id, username, password_hash, role, ref_id)
VALUES
    (6, 'coach3', '62d88c11caf634949b6feee663772872cfc30b308e180c52fdf6a4d13b8572ab', 'COACH', 6),
    (7, 'coach4', '6c1e69b99e04d21af6636e86c68931d45bc850f1d6b3bf93b2a28def777abe67', 'COACH', 7),
    (8, 'coach5', '08df9f25bfae761856bbb42fe13382c7ef685db9935b49bd114292a342dd6721', 'COACH', 8),
    (9, 'member3', '7dc45c6858531fd086309e25a5f420dfa3bc6e78fe57cb6a1ad7379172411ed8', 'MEMBER', 6),
    (10, 'member4', '112025b57e89d507a1a2e4a70a99e341c166900c78d63c6823098715150321c6', 'MEMBER', 7),
    (11, 'member5', 'd55803349f2b570383dfa8606ceb3ed8ad5985d2ed98bfde57b0221739244860', 'MEMBER', 8),
    (12, 'member6', 'fdb69b7b0f03ab978a9b3219c2bef642a6df0de8cefc869a7041845284a5ccd8', 'MEMBER', 9),
    (13, 'member7', '3cb2ae7a50052d2677c817f48d038a16617735dbd9786f6a5fd432915dafa7ac', 'MEMBER', 10);

-- 增加更多预约 (ID 4-15)
INSERT IGNORE INTO booking (id, schedule_id, member_id, status, created_at, amount, attendance_status, rating, review_content, reviewed_at)
VALUES
    (4, 4, 3, 'CANCELLED', DATE_SUB(NOW(), INTERVAL 3 DAY), 50.00, 'SCHEDULED', NULL, NULL, NULL), -- 已取消
    (5, 5, 4, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY), 50.00, 'ABSENT', NULL, NULL, NULL),    -- 缺勤
    (6, 1, 3, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY), 50.00, 'ATTENDED', 3, '人有点多，教练照顾不过来。', DATE_SUB(NOW(), INTERVAL 10 HOUR)), -- 低分评价
    (7, 2, 5, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY), 50.00, 'ATTENDED', 5, '非常专业的普拉提入门！', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (8, 6, 6, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 12 HOUR), 80.00, 'SCHEDULED', NULL, NULL, NULL), -- 待出勤
    (9, 15, 9, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 HOUR), 250.00, 'SCHEDULED', NULL, NULL, NULL), -- 满员测试1
    (10, 15, 10, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 HOUR), 250.00, 'SCHEDULED', NULL, NULL, NULL), -- 满员测试2 (ID 15 capacity is 2)
    (11, 7, 9, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY), 250.00, 'ATTENDED', 5, '私教器械非常专业，效果明显。', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
    (12, 8, 6, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 2 DAY), 40.00, 'ATTENDED', 4, '音乐好听，跳得很累。', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
    (13, 9, 3, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 3 DAY), 60.00, 'ATTENDED', 5, '极限挑战，真的快练废了，爽！', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (14, 10, 1, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 4 DAY), 50.00, 'ATTENDED', 2, '教练迟到了5分钟。', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (15, 11, 2, 'COMPLETED', DATE_SUB(NOW(), INTERVAL 5 DAY), 50.00, 'ABSENT', NULL, '临时有事没去成，可惜了。', DATE_SUB(NOW(), INTERVAL 4 DAY));

