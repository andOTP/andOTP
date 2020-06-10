/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
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

import org.shadowice.flocke.andotp.R;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

public class KeyStoreHelper {

    public static void wipeKeys(Context context) {
        File keyFile = new File(context.getFilesDir() + "/" + Constants.FILENAME_ENCRYPTED_KEY);
        keyFile.delete();

        try {
            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.containsAlias(Constants.KEYSTORE_ALIAS_WRAPPING))
                keyStore.deleteEntry(Constants.KEYSTORE_ALIAS_WRAPPING);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

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

        if (entry != null)
            return new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
        else
            return null;
    }

    public static SecretKey loadEncryptionKeyFromKeyStore(Context context, boolean failSilent) {
        SecretKey encKey = null;

        try {
            KeyPair pair = KeyStoreHelper.loadOrGenerateAsymmetricKeyPair(context, Constants.KEYSTORE_ALIAS_WRAPPING);
            if (pair != null)
                encKey = EncryptionHelper.loadOrGenerateWrappedKey(new File(context.getFilesDir() + "/" + Constants.FILENAME_ENCRYPTED_KEY), pair);
        } catch (GeneralSecurityException | IOException | ProviderException e) {
            e.printStackTrace();
            if (! failSilent)
                UIHelper.showGenericDialog(context, R.string.dialog_title_keystore_error, R.string.dialog_msg_keystore_error);
        }

        return encKey;
    }
}
