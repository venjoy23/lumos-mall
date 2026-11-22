package com.lumos.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// MD5 加密工具（加盐）
public class MD5Utils {

    private static final String SALT = "lumos#2026";

    public static String encrypt(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest((SALT + raw).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }
}
