/*
 * Copyright (C) 2017 Jakob Nixdorf
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

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class TokenCalculator {
    public static final int TOTP_DEFAULT_PERIOD = 30;
    public static final int TOTP_DEFAULT_DIGITS = 6;

    public enum HashAlgorithm {
        SHA1, SHA256, SHA512
    }

    public static final HashAlgorithm DEFAULT_ALGORITHM = HashAlgorithm.SHA1;

    private static byte[] generateHash(HashAlgorithm algorithm, byte[] key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String algo = "Hmac" + algorithm.toString();

        Mac mac = Mac.getInstance(algo);
        mac.init(new SecretKeySpec(key, algo));

        return mac.doFinal(data);
    }

    public static String TOTP(byte[] secret, int period, int digits, HashAlgorithm algorithm) {
        return String.format("%0" + digits + "d", TOTP(secret, period, System.currentTimeMillis() / 1000, digits, algorithm));
    }

    public static int TOTP(byte[] key, int period, long time, int digits, HashAlgorithm algorithm)
    {
        int r = 0;

        try {
            long timeInterval = time / period;

            byte[] data = ByteBuffer.allocate(8).putLong(timeInterval).array();
            byte[] hash = generateHash(algorithm, key, data);

            int offset = hash[hash.length - 1] & 0xF;

            int binary = (hash[offset] & 0x7F) << 0x18;
            binary |= (hash[offset + 1] & 0xFF) << 0x10;
            binary |= (hash[offset + 2] & 0xFF) << 0x08;
            binary |= (hash[offset + 3] & 0xFF);

            int div = (int) Math.pow(10, digits);

            r  = binary % div;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return r;
    }
}
