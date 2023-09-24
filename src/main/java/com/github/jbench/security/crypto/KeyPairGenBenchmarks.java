package com.github.jbench.security.crypto;

import com.github.jbench.BenchmarkExecutor;
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
import java.security.spec.RSAKeyGenParameterSpec;

/**
 * The benchmarks for key pair generator.
 */
public class KeyPairGenBenchmarks {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @State(Scope.Benchmark)
    public static class KeyPairGenProvider {

        @Param({"JDK", "BC"})
        String product;

        @Param({"DH", "EC", "EdDSA", "RSA", "RSASSA-PSS"})
        String algorithm;

        String provider;
        KeyPairGenerator keyPairGen;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            provider = provider();
            keyPairGen = KeyPairGenerator.getInstance(algorithm, provider);
            init();
        }

        private String provider() {
            if (product.equals("JDK")) {
                if (algorithm.equals("DH")) {
                    return "SunJCE";
                } else if(algorithm.equals("EC") || algorithm.equals("EdDSA")) {
                    return "SunEC";
                }  else if(algorithm.contains("RSA")) {
                    return "SunRsaSign";
                } else {
                    throw new IllegalArgumentException(
                            "Unsupported algorithm: " + algorithm);
                }
            }

            return product;
        }

        private void init() throws Exception {
            if (product.equals("JDK")) {
                initJDK(keyPairGen);
            } else if (product.equals("BC")) {
                initBC(keyPairGen);
            }
        }

        private void initJDK(KeyPairGenerator keyPairGen) throws Exception {
            if (algorithm.equals("DH")) {
                keyPairGen.initialize(2048);
            } else if (algorithm.equals("EC")) {
                keyPairGen.initialize(new ECGenParameterSpec("SECP256R1"));
            } else if (algorithm.equals("EdDSA")) {
                keyPairGen.initialize(new ECGenParameterSpec("Ed25519"));
            } else if (algorithm.contains("RSA")) {
                keyPairGen.initialize(new RSAKeyGenParameterSpec(
                        2048, RSAKeyGenParameterSpec.F4));
            }
        }

        private void initBC(KeyPairGenerator keyPairGen) throws Exception {
            initJDK(keyPairGen);
        }
    }

    @Benchmark
    public KeyPair keyPairGen(KeyPairGenProvider provider) {
        return provider.keyPairGen.generateKeyPair();
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkExecutor().execute(KeyPairGenBenchmarks.class);
    }
}
