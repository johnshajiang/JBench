package com.github.jbench;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * The utilities for the JMH-based benchmarks.
 */
public class BenchmarkUtils {

    public static final byte[] DATA_256B = bytes(256);
    public static final byte[] DATA_256KB = kbytes(256);
    public static final byte[] DATA_1MB = mbytes(1);

    public static final byte[] KEY_16 = bytes(16);
    public static final byte[] KEY_32 = bytes(32);

    public static final byte[] IV_16 = KEY_16;

    public static final SecretKey AES_KEY_16 = new SecretKeySpec(KEY_16, "AES");
    public static final SecretKey AES_KEY_32 = new SecretKeySpec(KEY_32, "AES");

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String toHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(Character.forDigit(b >> 4 & 0xF, 16));
            result.append(Character.forDigit(b & 0xF, 16));
        }
        return result.toString();
    }

    public static byte[] toBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Character.digit(hex.charAt(2 * i), 16);
            bytes[i] <<= 4;
            bytes[i] += Character.digit(hex.charAt(2 * i + 1), 16);
        }
        return bytes;
    }

    public static byte[] bytes(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = 'a';
        }
        return data;
    }

    public static byte[] kbytes(int sizeInKB) {
        return bytes(sizeInKB * 1024);
    }

    public static byte[] mbytes(int sizeInMB) {
        return kbytes(sizeInMB * 1024);
    }

    public static String formattedTime() {
        return formattedTime("yyyyMMddHHmmss");
    }

    public static String formattedTime(String pattern) {
        return LocalDateTime.now().format(
                DateTimeFormatter.ofPattern(pattern));
    }

    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
