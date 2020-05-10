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

package org.shadowice.flocke.andotp;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.KeyStoreHelper;
import org.shadowice.flocke.andotp.Utilities.TokenCalculator;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }

    public void testTOTPCalculation(){
        // Test Vectors from https://tools.ietf.org/html/rfc6238
        byte[] keySHA1 =  "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
        byte[] keySHA256 =  "12345678901234567890123456789012".getBytes(StandardCharsets.US_ASCII);
        byte[] keySHA512 =  "1234567890123456789012345678901234567890123456789012345678901234".getBytes(StandardCharsets.US_ASCII);

        assertEquals(94287082, TokenCalculator.TOTP_RFC6238(keySHA1,   TokenCalculator.TOTP_DEFAULT_PERIOD, 59L, 8, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals(46119246, TokenCalculator.TOTP_RFC6238(keySHA256, TokenCalculator.TOTP_DEFAULT_PERIOD, 59L, 8, TokenCalculator.HashAlgorithm.SHA256));
        assertEquals(90693936, TokenCalculator.TOTP_RFC6238(keySHA512, TokenCalculator.TOTP_DEFAULT_PERIOD, 59L, 8, TokenCalculator.HashAlgorithm.SHA512));

        assertEquals(7081804,  TokenCalculator.TOTP_RFC6238(keySHA1,   TokenCalculator.TOTP_DEFAULT_PERIOD, 1111111109L, 8, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals(68084774, TokenCalculator.TOTP_RFC6238(keySHA256, TokenCalculator.TOTP_DEFAULT_PERIOD, 1111111109L, 8, TokenCalculator.HashAlgorithm.SHA256));
        assertEquals(25091201, TokenCalculator.TOTP_RFC6238(keySHA512, TokenCalculator.TOTP_DEFAULT_PERIOD, 1111111109L, 8, TokenCalculator.HashAlgorithm.SHA512));

        assertEquals(14050471, TokenCalculator.TOTP_RFC6238(keySHA1,   TokenCalculator.TOTP_DEFAULT_PERIOD, 1111111111L, 8, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals(67062674, TokenCalculator.TOTP_RFC6238(keySHA256, TokenCalculator.TOTP_DEFAULT_PERIOD, 1111111111L, 8, TokenCalculator.HashAlgorithm.SHA256));
        assertEquals(99943326, TokenCalculator.TOTP_RFC6238(keySHA512, TokenCalculator.TOTP_DEFAULT_PERIOD, 1111111111L, 8, TokenCalculator.HashAlgorithm.SHA512));

        assertEquals(89005924, TokenCalculator.TOTP_RFC6238(keySHA1,   TokenCalculator.TOTP_DEFAULT_PERIOD, 1234567890L, 8, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals(91819424, TokenCalculator.TOTP_RFC6238(keySHA256, TokenCalculator.TOTP_DEFAULT_PERIOD, 1234567890L, 8, TokenCalculator.HashAlgorithm.SHA256));
        assertEquals(93441116, TokenCalculator.TOTP_RFC6238(keySHA512, TokenCalculator.TOTP_DEFAULT_PERIOD, 1234567890L, 8, TokenCalculator.HashAlgorithm.SHA512));

        assertEquals(69279037, TokenCalculator.TOTP_RFC6238(keySHA1,   TokenCalculator.TOTP_DEFAULT_PERIOD, 2000000000L, 8, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals(90698825, TokenCalculator.TOTP_RFC6238(keySHA256, TokenCalculator.TOTP_DEFAULT_PERIOD, 2000000000L, 8, TokenCalculator.HashAlgorithm.SHA256));
        assertEquals(38618901, TokenCalculator.TOTP_RFC6238(keySHA512, TokenCalculator.TOTP_DEFAULT_PERIOD, 2000000000L, 8, TokenCalculator.HashAlgorithm.SHA512));

        assertEquals(65353130, TokenCalculator.TOTP_RFC6238(keySHA1,   TokenCalculator.TOTP_DEFAULT_PERIOD, 20000000000L, 8, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals(77737706, TokenCalculator.TOTP_RFC6238(keySHA256, TokenCalculator.TOTP_DEFAULT_PERIOD, 20000000000L, 8, TokenCalculator.HashAlgorithm.SHA256));
        assertEquals(47863826, TokenCalculator.TOTP_RFC6238(keySHA512, TokenCalculator.TOTP_DEFAULT_PERIOD, 20000000000L, 8, TokenCalculator.HashAlgorithm.SHA512));
    }

    public void testHOTPCalculation() {
        // Test cases from https://tools.ietf.org/html/rfc4226
        byte[] keySHA1 = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

        assertEquals("755224", TokenCalculator.HOTP(keySHA1, 0, 6, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals("287082", TokenCalculator.HOTP(keySHA1, 1, 6, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals("359152", TokenCalculator.HOTP(keySHA1, 2, 6, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals("969429", TokenCalculator.HOTP(keySHA1, 3, 6, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals("338314", TokenCalculator.HOTP(keySHA1, 4, 6, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals("254676", TokenCalculator.HOTP(keySHA1, 5, 6, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals("287922", TokenCalculator.HOTP(keySHA1, 6, 6, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals("162583", TokenCalculator.HOTP(keySHA1, 7, 6, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals("399871", TokenCalculator.HOTP(keySHA1, 8, 6, TokenCalculator.HashAlgorithm.SHA1));
        assertEquals("520489", TokenCalculator.HOTP(keySHA1, 9, 6, TokenCalculator.HashAlgorithm.SHA1));
    }


    public void testEntry() throws Exception {
        byte secret[] = "Das System ist sicher".getBytes();
        String label = "5 von 5 Sterne";
        int period = 30;

        String s = "{\"secret\":\"" + new String(new Base32().encode(secret)) + "\"," +
                    "\"label\":\"" + label + "\"," +
                    "\"digits\":6," +
                    "\"type\":\"TOTP\"," +
                    "\"algorithm\":\"SHA1\"," +
                    "\"thumbnail\":\"Default\"," +
                    "\"last_used\":0," +
                    "\"period\":" + Integer.toString(period) + "," +
                    "\"tags\":[\"test1\",\"test2\"]}";

        Entry e = new Entry(new JSONObject(s));
        assertTrue(Arrays.equals(secret, e.getSecret()));
        assertEquals(label, e.getLabel());

        String[] tags = new String[]{"test1", "test2"};
        assertEquals(tags.length, e.getTags().size());
        assertTrue(Arrays.equals(tags, e.getTags().toArray(new String[e.getTags().size()])));

        assertEquals(s, e.toJSON().toString());
    }



    public void testEntryURL() throws Exception {
        try {
            new Entry("DON'T CARE");
            assertTrue(false);
        } catch (Exception e) {
        }

        try {
            new Entry("https://github.com/0xbb/");
            assertTrue(false);
        } catch (Exception e) {
        }

        try {
            new Entry("otpauth://hotp/ACME%20Co:john.doe@email.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ");
            assertTrue(false);
        }
        catch (Exception e){
        }

        try {
            new Entry("otpauth://totp/ACME");
            assertTrue(false);
        }
        catch (Exception e){
        }

        Entry entry = new Entry("otpauth://totp/ACME%20Co:john.doe@email.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&ALGORITHM=SHA1&digits=6&period=30");
        assertEquals("ACME Co - ACME Co:john.doe@email.com", entry.getLabel());

        assertEquals("HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ", new String(new Base32().encode(entry.getSecret())));


        entry = new Entry("otpauth://totp/ACME%20Co:john.doe@email.com?secret=HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ&issuer=ACME%20Co&ALGORITHM=SHA1&digits=6&period=30&tags=test1&tags=test2");
        assertEquals("ACME Co - ACME Co:john.doe@email.com", entry.getLabel());

        assertEquals("HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ", new String(new Base32().encode(entry.getSecret())));
        String[] tags = new String[]{"test1", "test2"};
        assertEquals(tags.length, entry.getTags().size());
        assertTrue(Arrays.equals(tags, entry.getTags().toArray(new String[entry.getTags().size()])));
    }

    public void testSettingsHelper() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        Context context = getContext();

        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        keyStore.deleteEntry("settings");

        new File(context.getFilesDir() + "/" + Constants.FILENAME_DATABASE).delete();
        new File(context.getFilesDir() + "/" + Constants.FILENAME_ENCRYPTED_KEY).delete();

        SecretKey encryptionKey = KeyStoreHelper.loadEncryptionKeyFromKeyStore(context, false);
        ArrayList<Entry> b = DatabaseHelper.loadDatabase(context, encryptionKey);
        assertEquals(0, b.size());

        ArrayList<Entry> a = new ArrayList<>();
        Entry e = new Entry();
        e.setLabel("label");
        e.setSecret("secret".getBytes());
        a.add(e);

        e = new Entry();
        e.setLabel("label2");
        e.setSecret("secret2".getBytes());
        a.add(e);

        DatabaseHelper.saveDatabase(context, a, encryptionKey);
        b = DatabaseHelper.loadDatabase(context, encryptionKey);

        assertEquals(a, b);

        new File(context.getFilesDir() + "/" + Constants.FILENAME_DATABASE).delete();
        new File(context.getFilesDir() + "/" + Constants.FILENAME_ENCRYPTED_KEY).delete();
    }

    public void testEncryptionHelper() throws NoSuchPaddingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException, DecoderException {


        // https://golang.org/src/crypto/cipher/gcm_test.go
        String[][] testCases =  new String[][]{
                new String []{"11754cd72aec309bf52f7687212e8957","3c819d9a9bed087615030b65","", "250327c674aaf477aef2675748cf6971" },
                new String []{"ca47248ac0b6f8372a97ac43508308ed","ffd2b598feabc9019262d2be","", "60d20404af527d248d893ae495707d1a" },
                new String []{"7fddb57453c241d03efbed3ac44e371c","ee283a3fc75575e33efd4887","d5de42b461646c255c87bd2962d3b9a2", "2ccda4a5415cb91e135c2a0f78c9b2fdb36d1df9b9d5e596f83e8b7f52971cb3" },
                new String []{"ab72c77b97cb5fe9a382d9fe81ffdbed","54cc7dc2c37ec006bcc6d1da","007c5e5b3e59df24a7c355584fc1518d", "0e1bde206a07a9c2c1b65300f8c649972b4401346697138c7a4891ee59867d0c" },
                new String []{"feffe9928665731c6d6a8f9467308308","cafebabefacedbaddecaf888","d9313225f88406e5a55909c5aff5269a86a7a9531534f7da2e4c303d8a318a721c3c0c95956809532fcf0e2449a6b525b16aedf5aa0de657ba637b391aafd255", "42831ec2217774244b7221b784d0d49ce3aa212f2c02a4e035c17e2329aca12e21d514b25466931c7d8f6a5aac84aa051ba30b396a0aac973d58e091473f59854d5c2af327cd64a62cf35abd2ba6fab4" },

        };

        for(String[] testCase: testCases){

                SecretKeySpec k = new SecretKeySpec(new Hex().decode(testCase[0].getBytes()), "AES");
                IvParameterSpec iv = new IvParameterSpec(new Hex().decode(testCase[1].getBytes()));

                byte[] cipherTExt = EncryptionHelper.encrypt(k,iv,new Hex().decode(testCase[2].getBytes()));
                String cipher = new String(new Hex().encode(cipherTExt));

                assertEquals(cipher, testCase[3]);

                assertEquals(testCase[2], new String(new Hex().encode(EncryptionHelper.decrypt(k, iv, cipherTExt))));

        }
    }


}