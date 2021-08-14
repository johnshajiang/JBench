package com.github.jcabench;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

/**
 * The benchmarks for MAC algorithms.
 */
public class MacBenchmarks {

    private final static byte[] MESSAGE = BenchmarkUtils.DATA_1MB;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @State(Scope.Benchmark)
    public static class MAC {

        @Param({"JDK", "BC"})
        String provider;

        @Param({"HmacSHA1", "HmacSHA224", "HmacSHA256", "HmacSHA384",
                "HmacSHA512/224", "HmacSHA512/256", "HmacSHA3-224",
                "HmacSHA3-256", "HmacSHA3-384", "HmacSHA3-512"})
        String algorithm;

        Mac self;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            self = Mac.getInstance(algorithm, provider());
            SecretKeySpec key = new SecretKeySpec(BenchmarkUtils.KEY_16, "AES");
            self.init(key);
        }

        private String provider() {
            return "JDK".equalsIgnoreCase(provider) ? "SunJCE" : provider;
        }
    }

    @Benchmark
    public byte[] mac(MAC mac) {
        return mac.self.doFinal(MESSAGE);
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkExecutor().execute(MacBenchmarks.class);
    }
}
