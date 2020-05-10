/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.shadowice.flocke.andotp.Utilities;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 * Wraps {@link SecretKey} instances using a public/private key pair stored in
 * the platform {@link KeyStore}. This allows us to protect symmetric keys with
 * hardware-backed crypto, if provided by the device.
 * <p>
 * See <a href="http://en.wikipedia.org/wiki/Key_Wrap">key wrapping</a> for more
 * details.
 * <p>
 * Not inherently thread safe.
 */
public class SecretKeyWrapper {
    private final Cipher mCipher;
    private final KeyPair mPair;

    /**
     * Create a wrapper using the public/private key pair with the given alias.
     * If no pair with that alias exists, it will be generated.
     */
    @SuppressLint("GetInstance")
    public SecretKeyWrapper(KeyPair keyPair)
            throws GeneralSecurityException, IOException {
        mCipher = Cipher.getInstance(Constants.ALGORITHM_ASYMMETRIC);
        mPair = keyPair;
    }

    /**
     * Wrap a {@link SecretKey} using the public key assigned to this wrapper.
     * Use {@link #unwrap(byte[])} to later recover the original
     * {@link SecretKey}.
     *
     * @return a wrapped version of the given {@link SecretKey} that can be
     *         safely stored on untrusted storage.
     */
    public byte[] wrap(SecretKey key)
            throws GeneralSecurityException {
        mCipher.init(Cipher.WRAP_MODE, mPair.getPublic());
        return mCipher.wrap(key);
    }

    /**
     * Unwrap a {@link SecretKey} using the private key assigned to this
     * wrapper.
     *
     * @param blob a wrapped {@link SecretKey} as previously returned by
     *            {@link #wrap(SecretKey)}.
     */
    public SecretKey unwrap(byte[] blob)
            throws GeneralSecurityException {
        mCipher.init(Cipher.UNWRAP_MODE, mPair.getPrivate());

        return (SecretKey) mCipher.unwrap(blob, "AES", Cipher.SECRET_KEY);
    }
}