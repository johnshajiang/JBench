package com.github.jcabench;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

/**
 * The benchmarks for AES and the associated operation modes.
 */
public class AesBenchmarks {

    private final static byte[] MESSAGE = BenchmarkUtils.DATA_1MB;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @State(Scope.Benchmark)
    public abstract static class CipherProvider {

        @Param({"JDK", "BC"})
        String product;

        @Param({"AES/CBC/NoPadding",
                "AES/CBC/PKCS5Padding",
                "AES/CTR/NoPadding",
                "AES/ECB/NoPadding",
                "AES/ECB/PKCS5Padding",
                "AES/GCM/NoPadding"})
        String transformation;

        String provider;
        Cipher cipher;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            provider = provider();
            cipher = cipher(opmode());
        }

        private String provider() {
            return "JDK".equalsIgnoreCase(product) ? "SunJCE" : product;
        }

        Cipher cipher(int opmode) throws Exception {
            Cipher cipher = Cipher.getInstance(transformation, provider);

            AlgorithmParameterSpec paramSpec = paramSpec();
            if (paramSpec == null) {
                cipher.init(opmode, BenchmarkUtils.AES_KEY_16);
            } else {
                cipher.init(opmode, BenchmarkUtils.AES_KEY_16, paramSpec);
            }

            return cipher;
        }

        abstract int opmode();

        private AlgorithmParameterSpec paramSpec() {
            if (transformation.contains("CBC") || transformation.contains("CTR")) {
                return BenchmarkUtils.IV_PARAM_16;
            } else if (transformation.contains("ECB")) {
                return null;
            } else if (transformation.contains("GCM")) {
                return BenchmarkUtils.GCM_PARAM_16;
            } else {
                throw new IllegalArgumentException(
                        "Unknown operation mode in the transformation: "
                                + transformation);
            }
        }
    }

    public static class Encrypter extends CipherProvider {

        int opmode() {
            return Cipher.ENCRYPT_MODE;
        }
    }

    public static class Decrypter extends CipherProvider {

        byte[] ciphertext;

        int opmode() {
            return Cipher.DECRYPT_MODE;
        }

        @Override
        public void setup() throws Exception {
            Cipher encrypter = cipher(Cipher.ENCRYPT_MODE);
            ciphertext = encrypter.doFinal(MESSAGE);
            super.setup();
        }
    }

    @Benchmark
    public byte[] encrypt(Encrypter encrypter) throws Exception {
        // GCM requires either IV or Key must NOT be reused.
        if (encrypter.transformation.contains("GCM")) {
            encrypter.cipher.init(Cipher.ENCRYPT_MODE, BenchmarkUtils.AES_KEY_16,
                    new GCMParameterSpec(128, BenchmarkUtils.randomBytes(16)));
        }
        return encrypter.cipher.doFinal(MESSAGE);
    }

    @Benchmark
    public byte[] decrypt(Decrypter decrypter) throws Exception {
        return decrypter.cipher.doFinal(decrypter.ciphertext);
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkExecutor().execute(AesBenchmarks.class);
    }
}
