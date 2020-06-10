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

package org.shadowice.flocke.andotp.Database;

import android.net.Uri;

import org.apache.commons.codec.binary.Base32;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shadowice.flocke.andotp.Utilities.EntryThumbnail;
import org.shadowice.flocke.andotp.Utilities.TokenCalculator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Entry {
    public enum OTPType {
        TOTP, HOTP, STEAM
    }

    private static final OTPType DEFAULT_TYPE = OTPType.TOTP;
    private static final int DEFAULT_PERIOD = 30;

    private static final String JSON_SECRET = "secret";
    private static final String JSON_ISSUER = "issuer";
    private static final String JSON_LABEL = "label";
    private static final String JSON_PERIOD = "period";
    private static final String JSON_COUNTER = "counter";
    private static final String JSON_DIGITS = "digits";
    private static final String JSON_TYPE = "type";
    private static final String JSON_ALGORITHM = "algorithm";
    private static final String JSON_TAGS = "tags";
    private static final String JSON_THUMBNAIL = "thumbnail";
    private static final String JSON_LAST_USED = "last_used";
    private static final String JSON_USED_FREQUENCY = "used_frequency";

    private OTPType type = OTPType.TOTP;
    private int period = TokenCalculator.TOTP_DEFAULT_PERIOD;
    private int digits = TokenCalculator.TOTP_DEFAULT_DIGITS;
    private TokenCalculator.HashAlgorithm algorithm = TokenCalculator.DEFAULT_ALGORITHM;
    private byte[] secret;
    private long counter;
    private String issuer;
    private String label;
    private String currentOTP;
    private boolean visible = false;
    private Runnable hideTask = null;
    private long last_update = 0;
    private long last_used = 0;
    private long used_frequency = 0;
    public List<String> tags = new ArrayList<>();
    private EntryThumbnail.EntryThumbnails thumbnail = EntryThumbnail.EntryThumbnails.Default;
    private static final int COLOR_DEFAULT = 0;
    public static final int COLOR_RED = 1;
    private static final int EXPIRY_TIME = 8;
    private int color = COLOR_DEFAULT;

    public Entry(){}

    public Entry(OTPType type, String secret, int period, int digits, String issuer, String label, TokenCalculator.HashAlgorithm algorithm, List<String> tags) {
        this.type = type;
        this.secret = new Base32().decode(secret.toUpperCase());
        this.period = period;
        this.digits = digits;
        this.issuer = issuer;
        this.label = label;
        this.algorithm = algorithm;
        this.tags = tags;
        setThumbnailFromIssuer(issuer);
    }

    public Entry(OTPType type, String secret, long counter, int digits, String issuer, String label, TokenCalculator.HashAlgorithm algorithm, List<String> tags) {
        this.type = type;
        this.secret = new Base32().decode(secret.toUpperCase());
        this.counter = counter;
        this.digits = digits;
        this.issuer = issuer;
        this.label = label;
        this.algorithm = algorithm;
        this.tags = tags;
        setThumbnailFromIssuer(issuer);
    }

    public Entry(String contents) throws Exception {
        contents = contents.replaceFirst("otpauth", "http");
        Uri uri = Uri.parse(contents);
        URL url = new URL(contents);

        if(!url.getProtocol().equals("http")){
            throw new Exception("Invalid Protocol");
        }

        switch (url.getHost()) {
            case "totp":
                type = OTPType.TOTP;
                break;
            case "hotp":
                type = OTPType.HOTP;
                break;
            case "steam":
                type = OTPType.STEAM;
                break;
            default:
                throw new Exception("unknown otp type");
        }

        String secret = uri.getQueryParameter("secret");
        String label = uri.getPath().substring(1);

        String counter = uri.getQueryParameter("counter");
        String issuer = uri.getQueryParameter("issuer");
        String period = uri.getQueryParameter("period");
        String digits = uri.getQueryParameter("digits");
        String algorithm = uri.getQueryParameter("algorithm");
        List<String> tags = uri.getQueryParameters("tags");

        if (type == OTPType.HOTP) {
            if (counter != null) {
                this.counter = Long.parseLong(counter);
            } else {
                throw new Exception("missing counter for HOTP");
            }
        } else if (type == OTPType.TOTP || type == OTPType.STEAM) {
            if (period != null) {
                this.period = Integer.parseInt(period);
            } else {
                this.period = TokenCalculator.TOTP_DEFAULT_PERIOD;
            }
        }

        this.issuer = issuer;
        this.label = label;
        this.secret = new Base32().decode(secret.toUpperCase());

        if (digits != null) {
            this.digits = Integer.parseInt(digits);
        } else {
            this.digits = this.type == OTPType.STEAM ? TokenCalculator.STEAM_DEFAULT_DIGITS : TokenCalculator.TOTP_DEFAULT_DIGITS;
        }

        if (algorithm != null) {
            this.algorithm = TokenCalculator.HashAlgorithm.valueOf(algorithm.toUpperCase());
        } else {
            this.algorithm = TokenCalculator.DEFAULT_ALGORITHM;
        }

        if (tags != null) {
            this.tags = tags;
        } else {
            this.tags = new ArrayList<>();
        }

        if (issuer != null) {
            setThumbnailFromIssuer(issuer);
        }
    }

    public Entry (JSONObject jsonObj)
            throws Exception {
        this.secret = new Base32().decode(jsonObj.getString(JSON_SECRET).toUpperCase());
        this.label = jsonObj.getString(JSON_LABEL);

        try {
            this.issuer = jsonObj.getString(JSON_ISSUER);
        } catch (JSONException e) {
            // Older backup version did not save issuer and label separately
            this.issuer = "";
        }

        try {
            this.type = OTPType.valueOf(jsonObj.getString(JSON_TYPE));
        } catch(Exception e) {
            this.type = DEFAULT_TYPE;
        }

        try {
            this.period = jsonObj.getInt(JSON_PERIOD);
        } catch(Exception e) {
            if (type == OTPType.TOTP)
                this.period = DEFAULT_PERIOD;
        }

        try {
            this.counter = jsonObj.getLong(JSON_COUNTER);
        } catch (Exception e) {
            if (type == OTPType.HOTP)
                throw new Exception("missing counter for HOTP");
        }

        try {
            this.digits = jsonObj.getInt(JSON_DIGITS);
        } catch(Exception e) {
            this.digits = TokenCalculator.TOTP_DEFAULT_DIGITS;
        }

        try {
            this.algorithm = TokenCalculator.HashAlgorithm.valueOf(jsonObj.getString(JSON_ALGORITHM));
        } catch (Exception e) {
            this.algorithm = TokenCalculator.DEFAULT_ALGORITHM;
        }

        this.tags = new ArrayList<>();
        try {
            JSONArray tagsArray = jsonObj.getJSONArray(JSON_TAGS);
            for(int i = 0; i < tagsArray.length(); i++) {
                this.tags.add(tagsArray.getString(i));
            }
        } catch (Exception e) {
            // Nothing wrong here
        }

        try {
            this.thumbnail = EntryThumbnail.EntryThumbnails.valueOfIgnoreCase(jsonObj.getString(JSON_THUMBNAIL));
        } catch(Exception e) {
            this.thumbnail = EntryThumbnail.EntryThumbnails.Default;
        }

        try {
            this.last_used = jsonObj.getLong(JSON_LAST_USED);
        } catch (Exception e) {
            this.last_used = 0;
        }

        try {
            this.used_frequency = jsonObj.getLong(JSON_USED_FREQUENCY);
        } catch (Exception e) {
            this.used_frequency = 0;
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(JSON_SECRET, new String(new Base32().encode(getSecret())));
        jsonObj.put(JSON_ISSUER, getIssuer());
        jsonObj.put(JSON_LABEL, getLabel());
        jsonObj.put(JSON_DIGITS, getDigits());
        jsonObj.put(JSON_TYPE, getType().toString());
        jsonObj.put(JSON_ALGORITHM, algorithm.toString());
        jsonObj.put(JSON_THUMBNAIL, getThumbnail().name());
        jsonObj.put(JSON_LAST_USED, getLastUsed());
        jsonObj.put(JSON_USED_FREQUENCY, getUsedFrequency() );

        if (type == OTPType.TOTP)
            jsonObj.put(JSON_PERIOD, getPeriod());
        else if (type == OTPType.HOTP)
            jsonObj.put(JSON_COUNTER, getCounter());

        JSONArray tagsArray = new JSONArray();
        for(String tag : tags){
            tagsArray.put(tag);
        }
        jsonObj.put(JSON_TAGS, tagsArray);

        return jsonObj;
    }

    public Uri toUri() {
        String type;
        switch (this.type) {
            case TOTP:
                type = "totp";
                break;
            case HOTP:
                type = "hotp";
                break;
            case STEAM:
                type = "steam";
                break;
            default:
                return null;
        }
        Uri.Builder builder = new Uri.Builder()
                .scheme("otpauth")
                .authority(type)
                .appendPath(this.label)
                .appendQueryParameter("secret", new Base32().encodeAsString(this.secret));
        if (this.issuer != null) {
            builder.appendQueryParameter("issuer", this.issuer);
        }
        switch (this.type) {
            case HOTP:
                builder.appendQueryParameter("counter", Long.toString(this.counter));
            case TOTP:
                if (this.period != TokenCalculator.TOTP_DEFAULT_PERIOD)
                    builder.appendQueryParameter("period", Integer.toString(this.period));
                break;
        }
        if (this.digits != TokenCalculator.TOTP_DEFAULT_DIGITS) {
            builder.appendQueryParameter("digits", Integer.toString(this.digits));
        }
        if (this.algorithm != TokenCalculator.DEFAULT_ALGORITHM) {
            builder.appendQueryParameter("algorithm", this.algorithm.name());
        }
        for (String tag : this.tags) {
            builder.appendQueryParameter("tags", tag);
        }
        return builder.build();
    }

    public boolean isTimeBased() {
        return type == OTPType.TOTP || type == OTPType.STEAM;
    }

    public boolean isCounterBased() { return type == OTPType.HOTP; }

    public OTPType getType() {
        return type;
    }

    public void setType(OTPType type) {
        this.type = type;
    }

    public byte[] getSecret() {
        return secret;
    }

    public String getSecretEncoded() {
        return new String(new Base32().encode(secret));
    }

    public void setSecret(byte[] secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
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

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) { this.tags = tags; }

    public EntryThumbnail.EntryThumbnails getThumbnail() { return thumbnail; }

    public void setThumbnail( EntryThumbnail.EntryThumbnails value) { thumbnail = value; }

    public TokenCalculator.HashAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    public void setAlgorithm(TokenCalculator.HashAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public boolean hasNonDefaultPeriod() {
        return this.period != TokenCalculator.TOTP_DEFAULT_PERIOD;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean value) {
        this.visible = value;
    }

    public void setHideTask(Runnable newTask) {
        this.hideTask = newTask;
    }

    public Runnable getHideTask() {
        return this.hideTask;
    }

    public long getLastUsed() {
        return this.last_used;
    }

    public void setLastUsed(long value) {
        this.last_used = value;
    }

    public long getUsedFrequency() {
        return used_frequency;
    }

    public void setUsedFrequency(long value) {
        this.used_frequency = value;
    }

    public String getCurrentOTP() {
        return currentOTP;
    }

    public boolean updateOTP() {
        if (type == OTPType.TOTP || type == OTPType.STEAM) {
            long time = System.currentTimeMillis() / 1000;
            long counter = time / this.getPeriod();

            if (counter > last_update) {
                if (type == OTPType.TOTP)
                    currentOTP = TokenCalculator.TOTP_RFC6238(secret, period, digits, algorithm);
                else if (type == OTPType.STEAM)
                    currentOTP = TokenCalculator.TOTP_Steam(secret, period, digits, algorithm);

                last_update = counter;
                //New OTP. Change color to default color
                setColor(COLOR_DEFAULT);
                return true;
            } else {
                return false;
            }
        } else if (type == OTPType.HOTP) {
            currentOTP = TokenCalculator.HOTP(secret, counter, digits, algorithm);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the OTP is expiring. The color for the entry will be changed to red if the expiry time is less than or equal to 8 seconds
     * COLOR_DEFAULT indicates that the OTP has not expired. In this case check if the OTP is about to expire. Update color to COLOR_RED if it's about to expire
     * COLOR_RED indicates that the OTP is already about to expire. Don't check again.
     * The color will be reset to COLOR_DEFAULT in {@link #updateOTP()} method
     *
     * @return Return true only if the color has changed to red to save from unnecessary notifying dataset
     * */
    public boolean hasColorChanged() {
        if(color == COLOR_DEFAULT){
            long time = System.currentTimeMillis() / 1000;
            if ((time % getPeriod()) > (getPeriod() - EXPIRY_TIME)) {
                setColor(COLOR_RED);
                return true;
            }
        }
        return false;
    }

    private void setThumbnailFromIssuer(String issuer) {
        try {
            this.thumbnail = EntryThumbnail.EntryThumbnails.valueOfIgnoreCase(issuer);
        } catch(Exception e) {
            this.thumbnail = EntryThumbnail.EntryThumbnails.Default;
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
                counter == entry.counter &&
                algorithm == entry.algorithm &&
                Arrays.equals(secret, entry.secret) &&
                Objects.equals(label, entry.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, period, counter, digits, algorithm, secret, label);
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public static boolean validateSecret(String secret) {
        try {
            new Base32().decode(secret.toUpperCase());
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
