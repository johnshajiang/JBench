package com.github.jcabench;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

/**
 * The utilities for the JMH-based benchmarks.
 */
public class BenchmarkUtils {

    public static final byte[] BYTE_256 = bytes(256);
    public static final byte[] KBYTE_256 = kbytes(256);
    public static final byte[] MBYTE_10 = mbytes(10);

    public static final byte[] KEY_16 = bytes(16);
    public static final byte[] KEY_32 = bytes(32);

    private static final HexFormat HEX = HexFormat.of();

    public static String toHex(byte[] bytes) {
        return HEX.formatHex(bytes);
    }

    public static byte[] toBytes(String hex) {
        return HEX.parseHex(hex);
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
}
