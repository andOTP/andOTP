/*
 * Copyright (C) 2017 Jakob Nixdorf
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
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.shadowice.flocke.andotp.R;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.shadowice.flocke.andotp.Preferences.PasswordEncryptedPreference.KEY_ALIAS;

public class Settings {
    private static final String DEFAULT_BACKUP_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "andOTP";

    private Context context;
    private SharedPreferences settings;

    public enum AuthMethod {
        NONE, PASSWORD, PIN, DEVICE
    }

    public enum SortMode {
        UNSORTED, LABEL
    }

    public Settings(Context context) {
        this.context = context;
        this.settings = PreferenceManager.getDefaultSharedPreferences(context);

        setupDeviceDependedDefaults();
        migrateDeprecatedSettings();
    }

    private void setupDeviceDependedDefaults() {
        if (! settings.contains(getResString(R.string.settings_key_backup_directory))
                || settings.getString(getResString(R.string.settings_key_backup_directory), "").isEmpty()) {
            setString(R.string.settings_key_backup_directory, DEFAULT_BACKUP_FOLDER);
        }
    }

    private void migrateDeprecatedSettings() {
        if (settings.contains(getResString(R.string.settings_key_auth_password))) {
            String plainPassword = getAuthPassword();
            String hashedPassword = new String(Hex.encodeHex(DigestUtils.sha256(plainPassword)));

            setString(R.string.settings_key_auth_password_hash, hashedPassword);

            remove(R.string.settings_key_auth_password);
        }

        if (settings.contains(getResString(R.string.settings_key_auth_pin))) {
            String plainPIN = getAuthPIN();
            String hashedPIN = new String(Hex.encodeHex(DigestUtils.sha256(plainPIN)));

            setString(R.string.settings_key_auth_pin_hash, hashedPIN);

            remove(R.string.settings_key_auth_pin);
        }

        if (settings.contains(getResString(R.string.settings_key_backup_password))) {
            String plainPassword = getBackupPassword();

            try {
                KeyPair key = KeyStoreHelper.loadOrGenerateAsymmetricKeyPair(context, KEY_ALIAS);
                byte[] encPassword = EncryptionHelper.encrypt(key.getPublic(), plainPassword.getBytes(StandardCharsets.UTF_8));

                setString(R.string.settings_key_backup_password_enc, Base64.encodeToString(encPassword, Base64.URL_SAFE));

                remove(R.string.settings_key_backup_password);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getResString(int resId) {
        return context.getString(resId);
    }

    private String getString(int keyId, int defaultId) {
        return settings.getString(getResString(keyId), getResString(defaultId));
    }

    private String getString(int keyId, String defaultValue) {
        return settings.getString(getResString(keyId), defaultValue);
    }

    private boolean getBoolean(int keyId, boolean defaultValue) {
        return settings.getBoolean(getResString(keyId), defaultValue);
    }

    private long getLong(int keyId, long defaultValue) {
        return settings.getLong(getResString(keyId), defaultValue);
    }

    private Set<String> getStringSet(int keyId, Set<String> defaultValue) {
        return new HashSet<String>(settings.getStringSet(getResString(keyId), defaultValue));
    }

    private void setBoolean(int keyId, boolean value) {
        settings.edit()
                .putBoolean(getResString(keyId), value)
                .apply();
    }

    private void setString(int keyId, String value) {
        settings.edit()
                .putString(getResString(keyId), value)
                .apply();
    }

    private void setStringSet(int keyId, Set<String> value) {
        settings.edit()
                .putStringSet(getResString(keyId), value)
                .apply();
    }

    private void remove(int keyId) {
        settings.edit()
                .remove(getResString(keyId))
                .apply();
    }

    public void clear(boolean keep_auth) {
        String authMethod = getAuthMethod().toString().toLowerCase();
        String authPassword = getAuthPasswordHash();
        String authPIN = getAuthPINHash();

        boolean warningShown = getFirstTimeWarningShown();

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();

        editor.putBoolean(getResString(R.string.settings_key_security_backup_warning), warningShown);

        if (keep_auth) {
            editor.putString(getResString(R.string.settings_key_auth), authMethod);

            if (!authPassword.isEmpty())
                editor.putString(getResString(R.string.settings_key_auth_password_hash), authPassword);

            if (!authPIN.isEmpty())
                editor.putString(getResString(R.string.settings_key_auth_pin_hash), authPIN);
        }

        editor.commit();

        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
    }



    public void registerPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        settings.registerOnSharedPreferenceChangeListener(listener);
    }



    public AuthMethod getAuthMethod() {
        String authString = getString(R.string.settings_key_auth, R.string.settings_default_auth);
        return AuthMethod.valueOf(authString.toUpperCase());
    }

    public String getAuthPassword() {
        return getString(R.string.settings_key_auth_password, "");
    }

    public String getAuthPasswordHash() {
        return getString(R.string.settings_key_auth_password_hash, "");
    }

    public String getAuthPIN() {
        return getString(R.string.settings_key_auth_pin, "");
    }

    public String getAuthPINHash() {
        return getString(R.string.settings_key_auth_pin_hash, "");
    }

    public Set<String> getPanicResponse() {
        return settings.getStringSet(getResString(R.string.settings_key_panic), Collections.<String>emptySet());
    }

    public Locale getLang() {
        String lang = getString(R.string.settings_key_lang, R.string.settings_default_lang);

        if (lang.equals("system"))
            return Tools.getSystemLocale();
        else
            return new Locale(lang);
    }

    public String getTheme() {
        return getString(R.string.settings_key_theme, R.string.settings_default_theme);
    }

    public boolean getScrollLabel() {
        return getBoolean(R.string.settings_key_label_scroll, false);
    }

    public boolean getFirstTimeWarningShown() {
        return getBoolean(R.string.settings_key_security_backup_warning, false);
    }

    public void setFirstTimeWarningShown(boolean value) {
        setBoolean(R.string.settings_key_security_backup_warning, value);
    }

    public boolean getSpecialFeatures() {
        return getBoolean(R.string.settings_key_special_features, false);
    }

    public void setSpecialFeatures(boolean value) {
        setBoolean(R.string.settings_key_special_features, value);
    }

    public SortMode getSortMode() {
        String modeStr = getString(R.string.settings_key_sort_mode, SortMode.UNSORTED.toString());
        return SortMode.valueOf(modeStr);
    }

    public void setSortMode(SortMode value) {
        setString(R.string.settings_key_sort_mode, value.toString());
    }

    public boolean getBackupAsk() {
        return getBoolean(R.string.settings_key_backup_ask, true);
    }

    public String getBackupDir() {
        return getString(R.string.settings_key_backup_directory, DEFAULT_BACKUP_FOLDER);
    }

    public String getBackupPassword() {
        return getString(R.string.settings_key_backup_password, "");
    }

    public String getBackupPasswordEnc() {
        String base64Password = getString(R.string.settings_key_backup_password_enc, "");
        byte[] encPassword = Base64.decode(base64Password, Base64.URL_SAFE);

        String password = "";

        try {
            KeyPair key = KeyStoreHelper.loadOrGenerateAsymmetricKeyPair(context, KEY_ALIAS);
            password = new String(EncryptionHelper.decrypt(key.getPrivate(), encPassword), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return password;
    }

    public String getOpenPGPProvider() {
        return getString(R.string.settings_key_openpgp_provider, "");
    }

    public long getOpenPGPKey() {
        return getLong(R.string.settings_key_openpgp_keyid, 0);
    }

    public boolean getOpenPGPSign() {
        return getBoolean(R.string.settings_key_openpgp_sign, false);
    }

    public boolean getOpenPGPVerify() {
        return getBoolean(R.string.settings_key_openpgp_verify, false);
    }

    public boolean getAllTagsToggle() {
        return getBoolean(R.string.settings_key_all_tags_toggle, true);
    }

    public void setAllTagsToggle(Boolean value) {
        setBoolean(R.string.settings_key_all_tags_toggle, value);
    }

    public boolean getNoTagsToggle() {
        return getBoolean(R.string.settings_key_no_tags_toggle, true);
    }

    public void setNoTagsToggle(Boolean value) {
        setBoolean(R.string.settings_key_no_tags_toggle, value);
    }

    public boolean getTagToggle(String tag) {
        //The tag toggle holds tags that are unchecked in order to default to checked.
        Set<String> toggledTags = getStringSet(R.string.settings_key_tags_toggles, new HashSet<String>());
        return !toggledTags.contains(tag);
    }

    public void setTagToggle(String tag, Boolean value) {
        Set<String> toggledTags = getStringSet(R.string.settings_key_tags_toggles, new HashSet<String>());
        if(value)
            toggledTags.remove(tag);
        else
            toggledTags.add(tag);
        setStringSet(R.string.settings_key_tags_toggles, toggledTags);
    }

    public boolean getThumbnailVisible() {
        return getBoolean(R.string.settings_key_thumbnail_visible, true);
    }

    public int getThumbnailSize() {
        try {
            String dimen = getString(R.string.settings_key_thumbnail_size, context.getResources().getString(R.string.settings_default_thumbnail_size));
            Log.d("dimen", dimen);
            return DimensionConverter.stringToDimensionPixelSize(dimen, context.getResources().getDisplayMetrics());
        } catch(Exception e) {
            e.printStackTrace();
            return context.getResources().getDimensionPixelSize(R.dimen.card_thumbnail_size);
        }
    }
}
