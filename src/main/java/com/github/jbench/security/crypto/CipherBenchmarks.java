package com.github.jbench.security.crypto;

import com.github.jbench.BenchmarkExecutor;
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
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;

/**
 * The benchmarks for symmetric ciphers and the associated operation modes.
 */
public class CipherBenchmarks {

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
                "AES/GCM/NoPadding",
                "ChaCha20",
                "ChaCha20-Poly1305"})
        String transformation;

        byte[] iv;
        Cipher cipher;

        @Setup(Level.Invocation)
        public void setup() throws Exception {
            cipher = cipher(opmode());
        }

        Cipher cipher(int opmode) throws Exception {
            iv = iv(opmode);
            Key key = key(transformation);

            Cipher cipher = Cipher.getInstance(transformation, provider());

            AlgorithmParameterSpec paramSpec = paramSpec();
            if (paramSpec == null) {
                cipher.init(opmode, key);
            } else {
                cipher.init(opmode, key, paramSpec);
            }

            return cipher;
        }

        private String provider() {
            return "JDK".equalsIgnoreCase(product) ? "SunJCE" : product;
        }

        abstract int opmode();

        byte[] iv(int opmode) {
            if(opmode == Cipher.ENCRYPT_MODE) {
                return transformation.contains("ChaCha20")
                        ? BenchmarkUtils.randomBytes(12)
                        : BenchmarkUtils.randomBytes(16);
            }

            return iv;
        }

        Key key(String transformation) {
            return transformation.contains("ChaCha20")
                    ? BenchmarkUtils.AES_KEY_32 : BenchmarkUtils.AES_KEY_16;
        }

        private AlgorithmParameterSpec paramSpec() {
            if (transformation.contains("CBC") || transformation.contains("CTR")) {
                return new IvParameterSpec(iv);
            } else if (transformation.contains("ECB")) {
                return null;
            } else if (transformation.contains("GCM")) {
                return new GCMParameterSpec(128, iv);
            } else if (transformation.equals("ChaCha20")) {
                if (product.equals("JDK")) {
                    return new ChaCha20ParameterSpec(iv, 0);
                } else if (product.equals("BC")) {
                    return new AEADParameterSpec(iv, 128);
                }
            } else if (transformation.equals("ChaCha20-Poly1305")) {
                if (product.equals("JDK")) {
                    return new IvParameterSpec(iv);
                } else if (product.equals("BC")) {
                    return new AEADParameterSpec(iv, 128);
                }
            }

            throw new IllegalArgumentException(
                    "Unknown transformation: " + transformation);
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
        return encrypter.cipher.doFinal(MESSAGE);
    }

    @Benchmark
    public byte[] decrypt(Decrypter decrypter) throws Exception {
        return decrypter.cipher.doFinal(decrypter.ciphertext);
    }

    public static void main(String[] args) throws Exception {
        new BenchmarkExecutor().execute(CipherBenchmarks.class);
    }
}
