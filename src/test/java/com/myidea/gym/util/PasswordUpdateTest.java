package com.myidea.gym.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 辅助工具类：生成各用户的正确密码 Hash 并打印 SQL
 */
public class PasswordUpdateTest {

    @Test
    public void generatePasswordHashes() {
        Map<String, String> users = new LinkedHashMap<>();
        // 管理员
        users.put("admin", "admin123");
        
        // 教练 (1-5)
        for (int i = 1; i <= 5; i++) {
            users.put("coach" + i, "123456");
        }
        
        // 会员 (1-7)
        for (int i = 1; i <= 7; i++) {
            users.put("member" + i, "member123");
        }

        System.out.println("-- 自动生成的正确密码 Hash (基于 PasswordUtil.hash(username, rawPassword))");
        for (Map.Entry<String, String> entry : users.entrySet()) {
            String username = entry.getKey();
            String rawPassword = entry.getValue();
            String hash = PasswordUtil.hash(username, rawPassword);
            System.out.printf("Username: %-10s | Raw: %-10s | Hash: %s%n", username, rawPassword, hash);
        }
    }
}
