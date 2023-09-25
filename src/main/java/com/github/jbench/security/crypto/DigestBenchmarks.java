package com.github.jbench.security.crypto;

import com.github.jbench.BenchmarkExecutor;
import com.github.jbench.BenchmarkUtils;
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
    public static class DigestProvider {

        @Param({"JDK", "BC"})
        String product;

        @Param({"SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512/224",
                "SHA-512/256", "SHA3-224", "SHA3-256", "SHA3-384", "SHA3-512"})
        String algorithm;

        String provider;
        MessageDigest messageDigest;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            provider = provider();
            messageDigest = MessageDigest.getInstance(algorithm, provider);
        }

        private String provider() {
            return "JDK".equalsIgnoreCase(product) ? "SUN" : product;
        }
    }

    @Benchmark
    public byte[] digest(DigestProvider provider) {
        return provider.messageDigest.digest(MESSAGE);
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkExecutor().execute(DigestBenchmarks.class);
    }
}
