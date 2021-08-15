package com.github.jcabench;

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
import java.security.spec.RSAKeyGenParameterSpec;

/**
 * The benchmarks for key exchange algorithms.
 */
public class KeyExBenchmarks {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @State(Scope.Benchmark)
    public static class KeyEx {

        @Param({"JDK", "BC"})
        String provider;

        @Param({"DH", "ECDH", "XDH"})
        String algorithm;

        KeyPair keyPair;

        KeyAgreement self;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            keyPair = keyPair();
            self = KeyAgreement.getInstance(algorithm, provider());
        }

        private String provider() {
            if ("JDK".equals(provider)) {
                if ("DH".equals(algorithm)) {
                    return "SunJCE";
                } else {
                    return "SunEC";
                }
            }

            return provider;
        }

        private KeyPair keyPair() throws Exception {
            String algorithm = keyAlgorithm();

            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithm);
            switch (algorithm) {
                case "DH":
                    keyPairGen.initialize(2048);
                    break;
                case "RSA":
                case "RSASSA-PSS":
                    keyPairGen.initialize(new RSAKeyGenParameterSpec(
                            2048, RSAKeyGenParameterSpec.F4));
                    break;
                case "EC":
                    keyPairGen.initialize(new ECGenParameterSpec("SECP256R1"));
                    break;
                case "XDH":
                    keyPairGen.initialize(new NamedParameterSpec("X25519"));
                    break;
                case "EdDSA":
                    keyPairGen.initialize(new ECGenParameterSpec("Ed25519"));
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unsupported algorithm: " + algorithm);
            }

            return keyPairGen.generateKeyPair();
        }

        private String keyAlgorithm() {
            if ("ECDH".equals(algorithm)) {
                return "EC";
            }

            return algorithm;
        }
    }

    @Benchmark
    public byte[] keyEx(KeyEx keyEx) throws Exception {
        keyEx.self.init(keyEx.keyPair.getPrivate());
        keyEx.self.doPhase(keyEx.keyPair.getPublic(), true);
        return keyEx.self.generateSecret();
    }

    public static void main(String[] args) throws RunnerException {
        new BenchmarkExecutor().execute(KeyExBenchmarks.class);
    }
}
