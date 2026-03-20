package com.wealthwise.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtils {

    // ── Hash password using MD5 ───────────────────────────────────────────────
    public static String hash(String password) {
        try {
            MessageDigest md    = MessageDigest.getInstance("MD5");
            byte[]        bytes = md.digest(password.getBytes());
            StringBuilder sb    = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString(); // always 32 characters
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    // ── Verify plain text against stored hash ─────────────────────────────────
    public static boolean verify(String raw, String hashed) {
        return hash(raw).equals(hashed);
    }
}