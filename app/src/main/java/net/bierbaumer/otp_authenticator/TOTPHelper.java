package net.bierbaumer.otp_authenticator;

import org.apache.commons.codec.binary.Base32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class TOTPHelper {
    public static final String SHA1 = "HmacSHA1";

    public static String generate(byte[] secret) {
        return String.format("%06d", generate(secret, System.currentTimeMillis() / 1000, 6));
    }

    public static int generate(byte[] key, long t, int digits)
    {
        int r = 0;
        try {
            t /= 30;
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
