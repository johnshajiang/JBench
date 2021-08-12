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
    public abstract static class Aes {

        @Param({"SunJCE", "BC"})
        String provider;

        @Param({"AES/CBC/NoPadding",
                "AES/CBC/PKCS5Padding",
                "AES/CTR/NoPadding",
                "AES/ECB/NoPadding",
                "AES/ECB/PKCS5Padding",
                "AES/GCM/NoPadding"})
        String transformation;

        Cipher cipher;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            cipher = cipher(opmode());
        }

        Cipher cipher(int opmode) throws Exception {
            Cipher cipher = Cipher.getInstance(transformation, provider);

            AlgorithmParameterSpec paramSpec = paramSpec(transformation);
            if (paramSpec == null) {
                cipher.init(opmode, BenchmarkUtils.AES_KEY_16);
            } else {
                cipher.init(opmode, BenchmarkUtils.AES_KEY_16, paramSpec);
            }

            return cipher;
        }

        abstract int opmode();

        private AlgorithmParameterSpec paramSpec(String transformation) {
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

    @State(Scope.Benchmark)
    public static class AesEnc extends Aes {

        int opmode() {
            return Cipher.ENCRYPT_MODE;
        }
    }

    @State(Scope.Benchmark)
    public static class AesDec extends Aes {

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
    public byte[] encrypt(AesEnc aes) throws Exception {
        // GCM requires either IV or Key must NOT be reused.
        if (aes.transformation.contains("GCM")) {
            aes.cipher.init(Cipher.ENCRYPT_MODE, BenchmarkUtils.AES_KEY_16,
                    new GCMParameterSpec(128, BenchmarkUtils.randomBytes(16)));
        }
        return aes.cipher.doFinal(MESSAGE);
    }

    @Benchmark
    public byte[] decrypt(AesDec aes) throws Exception {
        return aes.cipher.doFinal(aes.ciphertext);
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkExecutor().execute(AesBenchmarks.class);
    }
}
