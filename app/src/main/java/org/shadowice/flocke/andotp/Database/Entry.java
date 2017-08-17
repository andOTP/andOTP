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

package org.shadowice.flocke.andotp.Database;

import android.net.Uri;

import org.apache.commons.codec.binary.Base32;
import org.json.JSONException;
import org.json.JSONObject;
import org.shadowice.flocke.andotp.Utilities.TokenCalculator;

import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

public class Entry {
    public enum OTPType { TOTP }

    private static final OTPType DEFAULT_TYPE = OTPType.TOTP;

    private static final String JSON_SECRET = "secret";
    private static final String JSON_LABEL = "label";
    private static final String JSON_PERIOD = "period";
    private static final String JSON_DIGITS = "digits";
    private static final String JSON_TYPE = "type";
    private static final String JSON_ALGORITHM = "algorithm";

    private OTPType type = OTPType.TOTP;
    private int period = TokenCalculator.TOTP_DEFAULT_PERIOD;
    private int digits = TokenCalculator.TOTP_DEFAULT_DIGITS;
    private TokenCalculator.HashAlgorithm algorithm = TokenCalculator.DEFAULT_ALGORITHM;
    private byte[] secret;
    private String label;
    private String currentOTP;
    private long last_update = 0;

    public Entry(){}

    public Entry(OTPType type, String secret, int period, int digits, String label, TokenCalculator.HashAlgorithm algorithm) {
        this.type = type;
        this.secret = new Base32().decode(secret.toUpperCase());
        this.period = period;
        this.digits = digits;
        this.label = label;
        this.algorithm = algorithm;
    }

    public Entry(String contents) throws Exception {
        contents = contents.replaceFirst("otpauth", "http");
        Uri uri = Uri.parse(contents);
        URL url = new URL(contents);

        if(!url.getProtocol().equals("http")){
            throw new Exception("Invalid Protocol");
        }

        if(url.getHost().equals("totp")){
            type = OTPType.TOTP;
        } else {
            throw new Exception();
        }

        String secret = uri.getQueryParameter("secret");
        String label = uri.getPath().substring(1);

        String issuer = uri.getQueryParameter("issuer");
        String period = uri.getQueryParameter("period");
        String digits = uri.getQueryParameter("digits");
        String algorithm = uri.getQueryParameter("algorithm");

        if(issuer != null){
            label = issuer +" - "+label;
        }

        this.label = label;
        this.secret = new Base32().decode(secret.toUpperCase());

        if (period != null) {
            this.period = Integer.parseInt(period);
        } else {
            this.period = TokenCalculator.TOTP_DEFAULT_PERIOD;
        }

        if (digits != null) {
            this.digits = Integer.parseInt(digits);
        } else {
            this.digits = TokenCalculator.TOTP_DEFAULT_DIGITS;
        }

        if (algorithm != null) {
            this.algorithm = TokenCalculator.HashAlgorithm.valueOf(algorithm.toUpperCase());
        } else {
            this.algorithm = TokenCalculator.DEFAULT_ALGORITHM;
        }
    }

    public Entry (JSONObject jsonObj) throws JSONException {
        this.secret = new Base32().decode(jsonObj.getString(JSON_SECRET));
        this.label = jsonObj.getString(JSON_LABEL);
        this.period = jsonObj.getInt(JSON_PERIOD);

        try {
            this.digits = jsonObj.getInt(JSON_DIGITS);
        } catch(JSONException e) {
            this.digits = TokenCalculator.TOTP_DEFAULT_DIGITS;
        }

        try {
            this.type = OTPType.valueOf(jsonObj.getString(JSON_TYPE));
        } catch(JSONException e) {
            this.type = DEFAULT_TYPE;
        }

        try {
            this.algorithm = TokenCalculator.HashAlgorithm.valueOf(jsonObj.getString(JSON_ALGORITHM));
        } catch (JSONException e) {
            this.algorithm = TokenCalculator.DEFAULT_ALGORITHM;
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(JSON_SECRET, new String(new Base32().encode(getSecret())));
        jsonObj.put(JSON_LABEL, getLabel());
        jsonObj.put(JSON_PERIOD, getPeriod());
        jsonObj.put(JSON_DIGITS, getDigits());
        jsonObj.put(JSON_TYPE, getType().toString());
        jsonObj.put(JSON_ALGORITHM, algorithm.toString());

        return jsonObj;
    }

    public OTPType getType() {
        return type;
    }

    public void setType(OTPType type) {
        this.type = type;
    }

    public byte[] getSecret() {
        return secret;
    }

    public void setSecret(byte[] secret) {
        this.secret = secret;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public TokenCalculator.HashAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(TokenCalculator.HashAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public boolean hasNonDefaultPeriod() {
        return this.period != TokenCalculator.TOTP_DEFAULT_PERIOD;
    }

    public boolean hasNonDefaultDigits() {
        return this.digits != TokenCalculator.TOTP_DEFAULT_DIGITS;
    }

    public String getCurrentOTP() {
        return currentOTP;
    }

    public boolean updateOTP() {
        long time = System.currentTimeMillis() / 1000;
        long counter = time / this.getPeriod();

        if (counter > last_update) {
            currentOTP = TokenCalculator.TOTP(secret, period, digits, algorithm);
            last_update = counter;

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entry entry = (Entry) o;
        return period == entry.period &&
                digits == entry.digits &&
                type == entry.type &&
                algorithm == entry.algorithm &&
                Arrays.equals(secret, entry.secret) &&
                Objects.equals(label, entry.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, period, digits, algorithm, secret, label);
    }
}
