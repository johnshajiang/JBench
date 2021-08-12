package com.github.jcabench;

import org.bouncycastle.jcajce.spec.AEADParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import javax.crypto.Cipher;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

/**
 * The benchmarks for ChaCha20.
 */
public class ChaCha20Benchmarks {

    private final static byte[] MESSAGE = BenchmarkUtils.DATA_1MB;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @State(Scope.Benchmark)
    public abstract static class ChaCha20 {

        @Param({"SunJCE", "BC"})
        String provider;

        @Param({"ChaCha20", "ChaCha20-Poly1305"})
        String algorithm;

        Cipher cipher;

        byte[] iv;

        @Setup(Level.Invocation)
        public void setup() throws Exception {
            cipher = cipher(opmode());
        }

        Cipher cipher(int opmode) throws Exception {
            if (opmode == Cipher.ENCRYPT_MODE) {
                iv = BenchmarkUtils.randomBytes(12);
            }

            Cipher cipher = Cipher.getInstance(algorithm, provider);
            cipher.init(opmode, BenchmarkUtils.AES_KEY_32, paramSpec(iv));

            return cipher;
        }

        private AlgorithmParameterSpec paramSpec(byte[] iv) {
            if ("SunJCE".equals(provider)) {
                return "ChaCha20-Poly1305".equals(algorithm)
                        ? new IvParameterSpec(iv)
                        : new ChaCha20ParameterSpec(iv, 0);
            } else {
                return new AEADParameterSpec(iv, 128);
            }
        }

        abstract int opmode();
    }

    @State(Scope.Benchmark)
    public static class ChaCha20Enc extends ChaCha20 {

        int opmode() {
            return Cipher.ENCRYPT_MODE;
        }
    }

    @State(Scope.Benchmark)
    public static class ChaCha20Dec extends ChaCha20 {

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
    public byte[] encrypt(ChaCha20Enc chacha20) throws Exception {
        return chacha20.cipher.doFinal(MESSAGE);
    }

    @Benchmark
    public byte[] decrypt(ChaCha20Dec chacha20) throws Exception {
        return chacha20.cipher.doFinal(chacha20.ciphertext);
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkExecutor().execute(ChaCha20Benchmarks.class);
    }
}
