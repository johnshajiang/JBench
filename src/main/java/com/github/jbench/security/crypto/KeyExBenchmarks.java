package com.github.jbench.security.crypto;

import com.github.jbench.BenchmarkExecutor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.RunnerException;

import javax.crypto.KeyAgreement;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.NamedParameterSpec;

/**
 * The benchmarks for key exchange algorithms.
 */
public class KeyExBenchmarks {

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

    public static void main(String[] args) throws RunnerException {
        new BenchmarkExecutor().execute(KeyExBenchmarks.class);
    }
}
