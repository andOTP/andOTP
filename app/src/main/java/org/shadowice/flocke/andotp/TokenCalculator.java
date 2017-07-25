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

package org.shadowice.flocke.andotp;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class TokenCalculator {
    public static final int TOTP_DEFAULT_PERIOD = 30;

    public static final String SHA1 = "HmacSHA1";

    public static String TOTP(byte[] secret) {
        return String.format("%06d", TOTP(secret, TOTP_DEFAULT_PERIOD, System.currentTimeMillis() / 1000, 6));
    }

    public static String TOTP(byte[] secret, int period) {
        return String.format("%06d", TOTP(secret, period, System.currentTimeMillis() / 1000, 6));
    }

    public static int TOTP(byte[] key, int period, long t, int digits)
    {
        int r = 0;
        try {
            t /= period;
            byte[] data = new byte[8];
            long value = t;
            for (int i = 8; i-- > 0; value >>>= 8) {
                data[i] = (byte) value;
            }

            SecretKeySpec signKey = new SecretKeySpec(key, SHA1);
            Mac mac = Mac.getInstance(SHA1);
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);


            int offset = hash[20 - 1] & 0xF;

            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }

            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= Math.pow(10,digits);

            r  = (int) truncatedHash;
        }

        catch(Exception e){
        }

        return r;
    }
}
