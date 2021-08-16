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
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

/**
 * The benchmarks for signature algorithms.
 */
public class SignatureBenchmarks {

    private final static byte[] MESSAGE = BenchmarkUtils.DATA_1MB;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @State(Scope.Benchmark)
    public abstract static class Sig {

        @Param({"JDK", "BC"})
        String product;

        @Param({"SHA1withECDSA", "SHA256withECDSA", "SHA3-256withECDSA", "EdDSA",
                "SHA1withRSA", "SHA256withRSA", "SHA3-256withRSA", "RSASSA-PSS"})
        String algorithm;

        String provider;

        KeyPair keyPair;

        Signature self;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            provider = provider();
            keyPair = keyPair();
            self  = Signature.getInstance(algorithm, provider);
            initSignature();
        }

        void initSignature() throws Exception {
            if (algorithm.equals("RSASSA-PSS")) {
                self.setParameter(
                        new PSSParameterSpec("SHA-256", "MGF1",
                                MGF1ParameterSpec.SHA256, 20,
                                PSSParameterSpec.TRAILER_FIELD_BC));
            }
        }

        private String provider() {
            if ("JDK".equals(product)) {
                if(algorithm.contains("DSA")) {
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

        private KeyPair keyPair() throws Exception {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(
                    keyPairGenAlgo(), provider);

            if ("JDK".equals(product)) {
                initKePairJDK(keyPairGen);
            } else if ("BC".equals(product)) {
                initKeyPairBC(keyPairGen);
            }

            return keyPairGen.generateKeyPair();
        }

        private String keyPairGenAlgo() {
            if (algorithm.contains("ECDSA")) {
                return "EC";
            } else if (algorithm.contains("RSA")) {
                return "RSA";
            } else {
                return algorithm;
            }
        }

        private void initKePairJDK(KeyPairGenerator keyPairGen) throws Exception {
            if(algorithm.contains("ECDSA")) {
                keyPairGen.initialize(new ECGenParameterSpec("SECP256R1"));
            } else if (algorithm.equals("EdDSA")) {
                keyPairGen.initialize(new ECGenParameterSpec("Ed25519"));
            } else if(algorithm.contains("RSA")) {
                keyPairGen.initialize(new RSAKeyGenParameterSpec(
                        2048, RSAKeyGenParameterSpec.F4));
            }
        }

        private void initKeyPairBC(KeyPairGenerator keyPairGen) throws Exception {
            initKePairJDK(keyPairGen);
        }
    }

    public static class Signer extends Sig {

        @Override
        void initSignature() throws Exception {
            super.initSignature();
            self.initSign(keyPair.getPrivate());
        }
    }

    public static class Verifier extends Sig {

        byte[] signature;

        @Override
        void initSignature() throws Exception {
            super.initSignature();

            self.initSign(keyPair.getPrivate());
            self.update(MESSAGE, 0, MESSAGE.length);
            signature = self.sign();

            self.initVerify(keyPair.getPublic());
        }
    }

    @Benchmark
    public byte[] sign(Signer signer) throws SignatureException {
        signer.self.update(MESSAGE, 0, MESSAGE.length);
        return signer.self.sign();
    }

    @Benchmark
    public boolean verify(Verifier verifier) throws SignatureException {
        verifier.self.update(MESSAGE, 0, MESSAGE.length);
        return verifier.self.verify(verifier.signature);
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkExecutor().execute(SignatureBenchmarks.class);
    }
}
