/*
 * Copyright (C) 2017 Jakob Nixdorf
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

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class KeyStoreHelper {
    private final static int KEY_LENGTH = 16;

    public static KeyPair loadOrGenerateAsymmetricKeyPair(Context context, String alias)
            throws GeneralSecurityException, IOException {
        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (! keyStore.containsAlias(alias)) {
            final Calendar start = new GregorianCalendar();
            final Calendar end = new GregorianCalendar();
            end.add(Calendar.YEAR, 100);

            AlgorithmParameterSpec spec;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                spec = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setCertificateSubject(new X500Principal("CN=" + alias))
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .setCertificateSerialNumber(BigInteger.ONE)
                        .setCertificateNotBefore(start.getTime())
                        .setCertificateNotAfter(end.getTime())
                        .build();
            } else {
                spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(alias)
                        .setSubject(new X500Principal("CN=" + alias))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();
            }

            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");

            gen.initialize(spec);
            gen.generateKeyPair();
        }

        final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
        return new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
    }

    /**
     * Load our symmetric secret key.
     * The symmetric secret key is stored securely on disk by wrapping
     * it with a public/private key pair, possibly backed by hardware.
     */
    public static SecretKey loadOrGenerateWrappedKey(Context context, File keyFile)
            throws GeneralSecurityException, IOException {
        final SecretKeyWrapper wrapper = new SecretKeyWrapper(context, "settings");

        // Generate secret key if none exists
        if (!keyFile.exists()) {
            final byte[] raw = new byte[KEY_LENGTH];
            new SecureRandom().nextBytes(raw);

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
