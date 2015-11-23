package net.bierbaumer.otp_authenticator;

import android.net.Uri;

import org.apache.commons.codec.binary.Base32;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Arrays;

public class Entry {
    public static final String JSON_SECRET = "secret";
    public static final String JSON_LABEL = "label";

    private byte[] secret;
    private String label;
    private String currentOTP;

    public Entry (){

    }

    public Entry(String contents) throws Exception {
        contents = contents.replaceFirst("otpauth", "http");
        Uri uri = Uri.parse(contents);
        URL url = new URL(contents);

        if(!url.getProtocol().equals("http")){
            throw new Exception("Invalid Protocol");
        }

        if(!url.getHost().equals("totp")){
            throw new Exception();
        }

        String secret = uri.getQueryParameter("secret");
        String label = uri.getPath().substring(1);

        String issuer = uri.getQueryParameter("issuer");

        if(issuer != null){
            label = issuer +" - "+label;
        }

        this.label = label;
        this.secret = new Base32().decode(secret.toUpperCase());
    }

    public Entry (JSONObject jsonObj ) throws JSONException {
        this.setSecret(new Base32().decode(jsonObj.getString(JSON_SECRET)));
        this.setLabel(jsonObj.getString(JSON_LABEL));
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(JSON_SECRET, new String(new Base32().encode(getSecret())));
        jsonObj.put(JSON_LABEL, getLabel());

        return jsonObj;
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

    public String getCurrentOTP() {
        return currentOTP;
    }

    public void setCurrentOTP(String currentOTP) {
        this.currentOTP = currentOTP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entry entry = (Entry) o;

        if (!Arrays.equals(secret, entry.secret)) return false;
        return !(label != null ? !label.equals(entry.label) : entry.label != null);

    }

    @Override
    public int hashCode() {
        int result = secret != null ? Arrays.hashCode(secret) : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }
}
