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
import android.preference.PreferenceManager;

import org.shadowice.flocke.andotp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class Settings {
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

    public void clear() {
        settings.edit().clear().commit();
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

    public String getAuthPIN() {
        return getString(R.string.settings_key_auth_pin, "");
    }

    public Set<String> getPanicResponse() {
        return settings.getStringSet(getResString(R.string.settings_key_panic), Collections.<String>emptySet());
    }

    public String getTheme() {
        return getString(R.string.settings_key_theme, R.string.settings_default_theme);
    }

    public boolean getFirstTimeWarningShown() {
        return getBoolean(R.string.settings_key_security_backup_warning, false);
    }

    public void setFirstTimeWarningShown(boolean value) {
        setBoolean(R.string.settings_key_security_backup_warning, value);
    }

    public SortMode getSortMode() {
        String modeStr = getString(R.string.settings_key_sort_mode, SortMode.UNSORTED.toString());
        return SortMode.valueOf(modeStr);
    }

    public void setSortMode(SortMode value) {
        setString(R.string.settings_key_sort_mode, value.toString());
    }

    public String getBackupPassword() {
        return getString(R.string.settings_key_backup_password, "");
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
}
