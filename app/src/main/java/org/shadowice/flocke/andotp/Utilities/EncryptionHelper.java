/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
 * Copyright (C) 2015 Bruno Bierbaumer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.shadowice.flocke.andotp.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionHelper {
    public static class PBKDF2Credentials {
        public byte[] password;
        public byte[] key;
    }

    public static int generateRandomIterations() {
        Random rand = new Random();
        return rand.nextInt((Constants.PBKDF2_MAX_ITERATIONS - Constants.PBKDF2_MIN_ITERATIONS) + 1) + Constants.PBKDF2_MIN_ITERATIONS;
    }

    public static byte[] generateRandom(int length) {
        final byte[] raw = new byte[length];
        new SecureRandom().nextBytes(raw);

        return raw;
    }

    public static PBKDF2Credentials generatePBKDF2Credentials(String password, byte[] salt, int iter)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iter, Constants.PBKDF2_LENGTH);

        byte[] array = secretKeyFactory.generateSecret(keySpec).getEncoded();

        int halfPoint = array.length / 2;

        PBKDF2Credentials credentials = new PBKDF2Credentials();
        credentials.password = Arrays.copyOfRange(array, halfPoint, array.length);
        credentials.key = Arrays.copyOfRange(array, 0, halfPoint);

        return credentials;
    }

    public static SecretKey generateSymmetricKey(byte[] data) {
        return new SecretKeySpec(data, 0, data.length, "AES");
    }

    public static SecretKey generateSymmetricKeyPBKDF2(String password, int iter, byte[] salt)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iter, Constants.PBKDF2_LENGTH);

        return secretKeyFactory.generateSecret(keySpec);
    }

    public static SecretKey generateSymmetricKeyFromPassword(String password)
            throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");

        return new SecretKeySpec(sha.digest(password.getBytes(StandardCharsets.UTF_8)), "AES");
    }

    public static byte[] encrypt(SecretKey secretKey, IvParameterSpec iv, byte[] plainText)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(Constants.ALGORITHM_SYMMETRIC);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        return cipher.doFinal(plainText);
    }

    public static byte[] encrypt(SecretKey secretKey, byte[] plaintext)
            throws NoSuchPaddingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        final byte[] iv = new byte[Constants.ENCRYPTION_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        byte[] cipherText = encrypt(secretKey, new IvParameterSpec(iv), plaintext);

        byte[] combined = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

        return combined;
    }

    public static byte[] encrypt(PublicKey publicKey, byte[] plaintext)
            throws NoSuchPaddingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(Constants.ALGORITHM_ASYMMETRIC);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(plaintext);
    }

    public static byte[] decrypt(SecretKey secretKey, IvParameterSpec iv, byte[] cipherText)
            throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(Constants.ALGORITHM_SYMMETRIC);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

        return cipher.doFinal(cipherText);
    }

    public static byte[] decrypt(SecretKey secretKey, byte[] cipherText)
            throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] iv = Arrays.copyOfRange(cipherText, 0, Constants.ENCRYPTION_IV_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(cipherText, Constants.ENCRYPTION_IV_LENGTH, cipherText.length);

        return decrypt(secretKey, new IvParameterSpec(iv), encrypted);
    }

    public static byte[] decrypt(PrivateKey privateKey, byte[] cipherText)
            throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(Constants.ALGORITHM_ASYMMETRIC);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(cipherText);
    }

    /**
     * Load our symmetric secret key.
     * The symmetric secret key is stored securely on disk by wrapping
     * it with a public/private key pair, possibly backed by hardware.
     */
    public static SecretKey loadOrGenerateWrappedKey(File keyFile, KeyPair keyPair)
            throws GeneralSecurityException, IOException {
        final SecretKeyWrapper wrapper = new SecretKeyWrapper(keyPair);

        // Generate secret key if none exists
        if (!keyFile.exists()) {
            final byte[] raw = EncryptionHelper.generateRandom(Constants.ENCRYPTION_KEY_LENGTH);

            final SecretKey key = new SecretKeySpec(raw, "AES");
            final byte[] wrapped = wrapper.wrap(key);


            FileHelper.writeBytesToFile(keyFile, wrapped);
        }

        // Even if we just generated the key, always read it back to ensure we
        // can read it successfully.
        final byte[] wrapped = FileHelper.readFileToBytes(keyFile);

        return wrapper.unwrap(wrapped);
    }
}
