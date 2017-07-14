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

import android.net.Uri;

import org.apache.commons.codec.binary.Base32;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Arrays;

import static org.shadowice.flocke.andotp.TOTPHelper.TOTP_DEFAULT_PERIOD;

public class Entry {
    public enum OTPType { TOTP, HOTP }

    private static final OTPType DEFAULT_TYPE = OTPType.TOTP;

    private static final String JSON_SECRET = "secret";
    private static final String JSON_LABEL = "label";
    private static final String JSON_PERIOD = "period";
    private static final String JSON_TYPE = "type";

    private OTPType type = OTPType.TOTP;
    private byte[] secret;
    private String label;
    private int period = TOTPHelper.TOTP_DEFAULT_PERIOD;
    private String currentOTP;
    private long last_update = 0;

    public Entry(){}

    public Entry(OTPType type, String secret, int period, String label) {
        this.type = type;
        this.secret = new Base32().decode(secret.toUpperCase());
        this.period = period;
        this.label = label;
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

        if(issuer != null){
            label = issuer +" - "+label;
        }

        this.label = label;
        this.secret = new Base32().decode(secret.toUpperCase());
        if (period != null) {
            this.period = Integer.parseInt(period);
        } else {
            this.period = TOTP_DEFAULT_PERIOD;
        }
    }

    public Entry (JSONObject jsonObj ) throws JSONException {
        this.setSecret(new Base32().decode(jsonObj.getString(JSON_SECRET)));
        this.setLabel(jsonObj.getString(JSON_LABEL));
        this.setPeriod(jsonObj.getInt(JSON_PERIOD));
        try {
            this.setType(jsonObj.getString(JSON_TYPE));
        } catch(JSONException e) {
            this.setType(DEFAULT_TYPE);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(JSON_SECRET, new String(new Base32().encode(getSecret())));
        jsonObj.put(JSON_LABEL, getLabel());
        jsonObj.put(JSON_PERIOD, getPeriod());
        jsonObj.put(JSON_TYPE, getType().toString());

        return jsonObj;
    }

    public OTPType getType() {
        return type;
    }

    public void setType(String type) {
        this.type = OTPType.valueOf(type);
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

    public int getPeriod() { return period; }

    public void setPeriod(int period) { this.period = period; }

    public boolean hasNonDefaultPeriod() {
        return this.period != TOTP_DEFAULT_PERIOD;
    }

    public String getCurrentOTP() {
        return currentOTP;
    }

    public void setCurrentOTP(String currentOTP) {
        this.currentOTP = currentOTP;
    }

    public boolean updateOTP() {
        long time = System.currentTimeMillis() / 1000;
        long counter = time / this.getPeriod();

        if (counter > this.last_update) {
            this.setCurrentOTP(TOTPHelper.generate(this.getSecret(), this.getPeriod()));
            this.last_update = counter;

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

        if (type != entry.type) return false;
        if (!Arrays.equals(secret, entry.secret)) return false;
        if (period != entry.period) return false;
        return !(label != null ? !label.equals(entry.label) : entry.label != null);

    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash + (secret == null ? 0 : Arrays.hashCode(secret));
        hash = hash + 13 * (label == null ? 0 : label.hashCode());
        hash = hash + 17 * type.hashCode();
        hash = hash + 31 * period;

        return hash;
    }
}
