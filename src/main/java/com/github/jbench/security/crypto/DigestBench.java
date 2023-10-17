package com.github.jbench.security.crypto;

import com.github.jbench.BenchmarkUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.security.MessageDigest;
import java.security.Security;
import java.util.concurrent.TimeUnit;

/**
 * The benchmarks for message digest algorithms.
 */
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 5, time = 10)
@Fork(value = 2, jvmArgsAppend = {"-server", "-Xms2048M", "-Xmx2048M", "-XX:+UseG1GC"})
@Threads(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class DigestBench {

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
}
