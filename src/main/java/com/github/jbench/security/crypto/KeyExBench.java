package com.github.jbench.security.crypto;

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

import javax.crypto.KeyAgreement;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.NamedParameterSpec;
import java.util.concurrent.TimeUnit;

/**
 * The benchmarks for key exchange algorithms.
 */
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 5, time = 10)
@Fork(value = 2, jvmArgsAppend = {"-server", "-Xms2048M", "-Xmx2048M", "-XX:+UseG1GC"})
@Threads(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class KeyExBench {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @State(Scope.Benchmark)
    public static class KeyExProvider {

        @Param({"JDK", "BC"})
        String product;

        @Param({"DH", "ECDH", "XDH"})
        String algorithm;

        String provider;
        KeyPair keyPair;
        KeyAgreement keyEx;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            provider = provider();
            keyPair = keyPair();
            keyEx = KeyAgreement.getInstance(algorithm, provider);
        }

        private String provider() {
            if (product.equals("JDK")) {
                if (algorithm.equals("DH")) {
                    return "SunJCE";
                } else {
                    return "SunEC";
                }
            }

            return product;
        }

        private KeyPair keyPair() throws Exception {
            String algorithm = keyAlgorithm();

            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithm);

            if(algorithm.equals("DH")) {
                keyPairGen.initialize(2048);
            } else if(algorithm.equals("EC")) {
                keyPairGen.initialize(new ECGenParameterSpec("SECP256R1"));
            } else if(algorithm.equals("XDH")) {
                keyPairGen.initialize(new NamedParameterSpec("X25519"));
            } else {
                throw new IllegalArgumentException(
                        "Unsupported key algorithm: " + algorithm);
            }

            return keyPairGen.generateKeyPair();
        }

        private String keyAlgorithm() {
            if (algorithm.equals("ECDH")) {
                return "EC";
            }

            return algorithm;
        }
    }

    @Benchmark
    public byte[] keyEx(KeyExProvider provider) throws Exception {
        provider.keyEx.init(provider.keyPair.getPrivate());
        provider.keyEx.doPhase(provider.keyPair.getPublic(), true);
        return provider.keyEx.generateSecret();
    }
}
