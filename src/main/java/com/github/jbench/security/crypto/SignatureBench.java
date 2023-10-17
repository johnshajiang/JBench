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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.concurrent.TimeUnit;

/**
 * The benchmarks for signature algorithms.
 */
@Warmup(iterations = 5, time = 5)
@Measurement(iterations = 5, time = 10)
@Fork(value = 2, jvmArgsAppend = {"-server", "-Xms2048M", "-Xmx2048M", "-XX:+UseG1GC"})
@Threads(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class SignatureBench {

    private final static byte[] MESSAGE = BenchmarkUtils.DATA_1MB;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @State(Scope.Benchmark)
    public abstract static class SignatureProvider {

        @Param({"JDK", "BC"})
        String product;

        @Param({"SHA1withECDSA", "SHA256withECDSA", "SHA3-256withECDSA", "EdDSA",
                "SHA1withRSA", "SHA256withRSA", "SHA3-256withRSA", "RSASSA-PSS"})
        String algorithm;

        String provider;
        KeyPair keyPair;
        Signature signature;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            provider = provider();
            keyPair = keyPair();
            signature = Signature.getInstance(algorithm, provider);
            initSignature();
        }

        void initSignature() throws Exception {
            if (algorithm.equals("RSASSA-PSS")) {
                signature.setParameter(
                        new PSSParameterSpec("SHA-256", "MGF1",
                                MGF1ParameterSpec.SHA256, 20,
                                PSSParameterSpec.TRAILER_FIELD_BC));
            }
        }

        private String provider() {
            if (product.equals("JDK")) {
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

            if (product.equals("JDK")) {
                initKePairJDK(keyPairGen);
            } else if (product.equals("BC")) {
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

    public static class Signer extends SignatureProvider {

        @Override
        void initSignature() throws Exception {
            super.initSignature();
            signature.initSign(keyPair.getPrivate());
        }
    }

    public static class Verifier extends SignatureProvider {

        byte[] sig;

        @Override
        void initSignature() throws Exception {
            super.initSignature();

            signature.initSign(keyPair.getPrivate());
            signature.update(MESSAGE, 0, MESSAGE.length);
            sig = signature.sign();

            signature.initVerify(keyPair.getPublic());
        }
    }

    @Benchmark
    public byte[] sign(Signer signer) throws SignatureException {
        signer.signature.update(MESSAGE, 0, MESSAGE.length);
        return signer.signature.sign();
    }

    @Benchmark
    public boolean verify(Verifier verifier) throws SignatureException {
        verifier.signature.update(MESSAGE, 0, MESSAGE.length);
        return verifier.signature.verify(verifier.sig);
    }
}
