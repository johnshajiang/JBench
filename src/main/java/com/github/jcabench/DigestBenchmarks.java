package com.github.jcabench;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.security.MessageDigest;
import java.security.Security;

/**
 * The benchmarks for message digest algorithms.
 */
public class DigestBenchmarks {

    private final static byte[] MESSAGE = BenchmarkUtils.DATA_1MB;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @State(Scope.Benchmark)
    public static class MD {

        @Param({"SUN", "BC"})
        String provider;

        @Param({"SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512/224",
                "SHA-512/256", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512"})
        String algorithm;

        MessageDigest self;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            self = MessageDigest.getInstance(algorithm, provider);
        }
    }

    @Benchmark
    public byte[] digest(MD md) {
        return md.self.digest(MESSAGE);
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkExecutor().execute(DigestBenchmarks.class);
    }
}
