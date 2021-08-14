package com.github.jcabench;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;

/**
 * The benchmarks for key pair generator.
 */
public class KeyPairGenBenchmarks {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @State(Scope.Benchmark)
    public static class KeyPairGen {

        @Param({"JDK", "BC"})
        String provider;

        @Param({"DiffieHellman", "EC", "EdDSA", "RSA", "RSASSA-PSS"})
        String algorithm;

        KeyPairGenerator self;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            self  = KeyPairGenerator.getInstance(algorithm, provider());
            init(self);
        }

        private String provider() {
            if ("JDK".equals(provider)) {
                if ("DiffieHellman".equals(algorithm)) {
                    return "SunJCE";
                } else if("EC".equals(algorithm) || "EdDSA".equals(algorithm)) {
                    return "SunEC";
                }  else if("RSA".equals(algorithm) || "RSASSA-PSS".equals(algorithm)) {
                    return "SunRsaSign";
                } else {
                    throw new IllegalArgumentException(
                            "Unsupported algorithm: " + algorithm);
                }
            }

            return provider;
        }

        private void init(KeyPairGenerator keyPairGen) throws Exception {
            if ("JDK".equals(provider)) {
                initJDK(keyPairGen);
            } else if ("BC".equals(provider)) {
                initBC(keyPairGen);
            }
        }

        private void initJDK(KeyPairGenerator keyPairGen) throws Exception {
            if ("DiffieHellman".equals(algorithm)
                    || "RSA".equals(algorithm)
                    || "RSASSA-PSS".equals(algorithm)) {
                keyPairGen.initialize(2048);
            } else if ("EC".equals(algorithm)) {
                keyPairGen.initialize(new ECGenParameterSpec("SECP256R1"));
            } else if ("EdDSA".equals(algorithm)) {
                keyPairGen.initialize(new ECGenParameterSpec("Ed25519"));
            }
        }

        private void initBC(KeyPairGenerator keyPairGen) throws Exception {
            initJDK(keyPairGen);
        }
    }

    @Benchmark
    public KeyPair keyPairGen(KeyPairGen keyPairGen) {
        return keyPairGen.self.generateKeyPair();
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkExecutor().execute(KeyPairGenBenchmarks.class);
    }
}
