package com.myidea.gym.util;

import cn.hutool.crypto.digest.DigestUtil;

public class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hash(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            throw new IllegalArgumentException("用户名或密码为空");
        }
        return DigestUtil.sha256Hex(username + ":" + rawPassword);
    }
}
