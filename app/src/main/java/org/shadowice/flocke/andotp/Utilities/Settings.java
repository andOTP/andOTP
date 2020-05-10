/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
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
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;

import org.shadowice.flocke.andotp.Preferences.CredentialsPreference;
import org.shadowice.flocke.andotp.R;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.shadowice.flocke.andotp.Utilities.Constants.AuthMethod;
import static org.shadowice.flocke.andotp.Utilities.Constants.EncryptionType;
import static org.shadowice.flocke.andotp.Utilities.Constants.SortMode;

public class Settings {
    private static final List<String> oldLangs = Arrays.asList("system", "en", "cs", "de", "es", "fr", "gl", "nl", "pl", "ru", "zh");
    private static final List<String> newLangs = Arrays.asList("system", "en_US", "cs_CZ", "de_DE", "es_ES", "fr_FR", "gl_ES", "nl_NL", "pl_PL", "ru_RU", "zh_CN");

    private Context context;
    private SharedPreferences settings;

    public Settings(Context context) {
        this.context = context;
        this.settings = PreferenceManager.getDefaultSharedPreferences(context);

        migrateDeprecatedSettings();
    }

    private void migrateDeprecatedSettings() {
        if (settings.contains(getResString(R.string.settings_key_auth_password))) {
            setAuthCredentials(getString(R.string.settings_key_auth_password, ""));
            remove(R.string.settings_key_auth_password);
        }

        if (settings.contains(getResString(R.string.settings_key_auth_pin))) {
            setAuthCredentials(getString(R.string.settings_key_auth_pin, ""));
            remove(R.string.settings_key_auth_pin);
        }

        if (settings.contains(getResString(R.string.settings_key_lang))) {
            String lang = getString(R.string.settings_key_lang, R.string.settings_default_locale);

            if (oldLangs.contains(lang))
                setLocale(newLangs.get(oldLangs.indexOf(lang)));

            remove(R.string.settings_key_lang);
        }

        if (settings.contains(getResString(R.string.settings_key_tap_to_reveal))) {
            if (getBoolean(R.string.settings_key_tap_to_reveal, false)) {
                setString(R.string.settings_key_tap_single, Constants.TapMode.REVEAL.toString().toLowerCase(Locale.ENGLISH));
            }
            remove(R.string.settings_key_tap_to_reveal);
        }

        if (settings.contains(getResString(R.string.settings_key_backup_password))) {
            String plainPassword = getBackupPassword();

            try {
                KeyPair key = KeyStoreHelper.loadOrGenerateAsymmetricKeyPair(context, Constants.KEYSTORE_ALIAS_PASSWORD);
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

    private int getResInt(int resId) {
        return context.getResources().getInteger(resId);
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

    private int getInt(int keyId, int defaultId) {
        return settings.getInt(getResString(keyId), getResInt(defaultId));
    }

    private int getIntValue(int keyId, int defaultValue) {
        return settings.getInt(getResString(keyId), defaultValue);
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

    private void setInt(int keyId, int value) {
        settings.edit()
                .putInt(getResString(keyId), value)
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
        AuthMethod authMethod = getAuthMethod();
        String authCredentials = getAuthCredentials();
        byte[] authSalt = getSalt();
        int authIterations = getIterations();

        boolean warningShown = getFirstTimeWarningShown();

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();

        editor.putBoolean(getResString(R.string.settings_key_security_backup_warning), warningShown);

        if (keep_auth) {
            editor.putString(getResString(R.string.settings_key_auth), authMethod.toString().toLowerCase());

            if (! authCredentials.isEmpty()) {
                editor.putString(getResString(R.string.settings_key_auth_credentials), authCredentials);
                editor.putInt(getResString(R.string.settings_key_auth_iterations), authIterations);

                String encodedSalt = Base64.encodeToString(authSalt, Base64.URL_SAFE);
                editor.putString(getResString(R.string.settings_key_auth_salt), encodedSalt);
            }
        }

        editor.commit();

        PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
    }



    public void registerPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        settings.registerOnSharedPreferenceChangeListener(listener);
    }



    public boolean getTapToReveal() {
        return getTapSingle() == Constants.TapMode.REVEAL || getTapDouble() == Constants.TapMode.REVEAL;
    }

    public int getTapToRevealTimeout() {
        return getInt(R.string.settings_key_tap_to_reveal_timeout, R.integer.settings_default_tap_to_reveal_timeout);
    }

    public AuthMethod getAuthMethod() {
        String authString = getString(R.string.settings_key_auth, CredentialsPreference.DEFAULT_VALUE.name().toLowerCase(Locale.ENGLISH));
        return AuthMethod.valueOf(authString.toUpperCase(Locale.ENGLISH));
    }

    public void setAuthMethod(AuthMethod authMethod) {
        setString(R.string.settings_key_auth, authMethod.name().toLowerCase(Locale.ENGLISH));
    }

    public void removeAuthPasswordHash() {
        remove(R.string.settings_key_auth_password_hash);
    }
    public void removeAuthPINHash() {
        remove(R.string.settings_key_auth_pin_hash);
    }

    public String getOldCredentials(AuthMethod method) {
        if (method == AuthMethod.PASSWORD)
            return getString(R.string.settings_key_auth_password_hash, "");
        else if (method == AuthMethod.PIN)
            return getString(R.string.settings_key_auth_pin_hash, "");
        else
            return "";
    }

    public String getAuthCredentials() {
        return getString(R.string.settings_key_auth_credentials, "");
    }

    public byte[] setAuthCredentials(String plainPassword) {
        byte[] key = null;

        try {
            int iterations = EncryptionHelper.generateRandomIterations();
            EncryptionHelper.PBKDF2Credentials credentials = EncryptionHelper.generatePBKDF2Credentials(plainPassword, getSalt(), iterations);
            String password = Base64.encodeToString(credentials.password, Base64.URL_SAFE);

            setIterations(iterations);
            setString(R.string.settings_key_auth_credentials, password);

            key = credentials.key;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return key;
    }

    public void setSalt(byte[] bytes) {
        String encodedSalt = Base64.encodeToString(bytes, Base64.URL_SAFE);
        setString(R.string.settings_key_auth_salt, encodedSalt);
    }

    public byte[] getSalt() {
        String storedSalt = getString(R.string.settings_key_auth_salt, "");

        if (storedSalt.isEmpty()) {
            byte[] newSalt = EncryptionHelper.generateRandom(Constants.PBKDF2_SALT_LENGTH);
            setSalt(newSalt);

            return newSalt;
        } else {
            return Base64.decode(storedSalt, Base64.URL_SAFE);
        }
    }

    public int getIterations() {
        return getIntValue(R.string.settings_key_auth_iterations, Constants.PBKDF2_DEFAULT_ITERATIONS);
    }

    public void setIterations(int value) {
        setInt(R.string.settings_key_auth_iterations, value);
    }

    public EncryptionType getEncryption() {
        String encType = getString(R.string.settings_key_encryption, R.string.settings_default_encryption);
        return EncryptionType.valueOf(encType.toUpperCase());
    }

    public void setEncryption(EncryptionType encryptionType) {
        setEncryption(encryptionType.name().toLowerCase());
    }

    public void setEncryption(String encryption) {
        setString(R.string.settings_key_encryption, encryption);
    }

    public Set<String> getPanicResponse() {
        return settings.getStringSet(getResString(R.string.settings_key_panic), Collections.<String>emptySet());
    }

    public boolean getRelockOnScreenOff() {
        return getBoolean(R.string.settings_key_relock_screen_off, true);
    }

    public boolean getRelockOnBackground() {
        return getBoolean(R.string.settings_key_relock_background, false);
    }

    public boolean getBlockAccessibility() {
        return getBoolean(R.string.settings_key_block_accessibility, false);
    }

    public void setLocale(String locale) {
        setString(R.string.settings_key_locale, locale);
    }

    public Locale getLocale() {
        String lang = getString(R.string.settings_key_locale, R.string.settings_default_locale);

        if (lang.equals("system")) {
            return Tools.getSystemLocale();
        } else {
            String[] splitLang =  lang.split("_");

            if (splitLang.length > 1) {
                return new Locale(splitLang[0], splitLang[1]);
            } else {
                return new Locale(lang);
            }
        }
    }

    public int getTheme() {
        String themeName = getString(R.string.settings_key_theme, R.string.settings_default_theme);

        int theme = R.style.AppTheme_NoActionBar;

        if (themeName.equals("light")) {
            theme = R.style.AppTheme_NoActionBar;
        } else if (themeName.equals("dark")) {
            theme = R.style.AppTheme_Dark_NoActionBar;
        } else if (themeName.equals("black")) {
            theme = R.style.AppTheme_Black_NoActionBar;
        }

        return theme;
    }

    public int getLabelSize() {
        return getInt(R.string.settings_key_label_size, R.integer.settings_default_label_size);
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

    public List<Constants.SearchIncludes> getSearchValues() {
        Set<String> stringValues = settings.getStringSet(getResString(R.string.settings_key_search_includes), new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.settings_defaults_search_includes))));

        List<Constants.SearchIncludes> values = new ArrayList<>();

        for (String value : stringValues) {
            values.add(Constants.SearchIncludes.valueOf(value.toUpperCase(Locale.ENGLISH)));
        }

        return values;
    }

    public Constants.CardLayouts getCardLayout() {
        String stringValue = getString(R.string.settings_key_card_layout, R.string.settings_default_card_layout);
        return Constants.CardLayouts.valueOf(stringValue.toUpperCase(Locale.ENGLISH));
    }

    public boolean getBackupAsk() {
        return getBoolean(R.string.settings_key_backup_ask, true);
    }

    public String getBackupDir() {
        return getString(R.string.settings_key_backup_directory, Constants.BACKUP_FOLDER);
    }

    public String getBackupPassword() {
        return getString(R.string.settings_key_backup_password, "");
    }

    public String getBackupPasswordEnc() {
        String base64Password = getString(R.string.settings_key_backup_password_enc, "");
        byte[] encPassword = Base64.decode(base64Password, Base64.URL_SAFE);

        String password = "";

        try {
            KeyPair key = KeyStoreHelper.loadOrGenerateAsymmetricKeyPair(context, Constants.KEYSTORE_ALIAS_PASSWORD);
            password = new String(EncryptionHelper.decrypt(key.getPrivate(), encPassword), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return password;
    }

    public Set<String> getBackupBroadcasts() {
        return settings.getStringSet(getResString(R.string.settings_key_backup_broadcasts), Collections.<String>emptySet());
    }

    public boolean isPlainTextBackupBroadcastEnabled() {
        return getBackupBroadcasts().contains("plain");
    }

    public boolean isEncryptedBackupBroadcastEnabled() {
        return getBackupBroadcasts().contains("encrypted");
    }

    public String getOpenPGPProvider() {
        return getString(R.string.settings_key_openpgp_provider, "");
    }

    public String getOpenPGPEncryptionUserIDs() {
        return getString(R.string.settings_key_openpgp_key_encrypt, "");
    }

    public long getOpenPGPSigningKey() {
        return getLong(R.string.settings_key_openpgp_key_sign, 0);
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
        return getThumbnailSize() > 0;
    }

    public int getThumbnailSize() {
        try {
            String dimen = getString(R.string.settings_key_thumbnail_size, context.getResources().getString(R.string.settings_default_thumbnail_size));
            return DimensionConverter.stringToDimensionPixelSize(dimen, context.getResources().getDisplayMetrics());
        } catch(Exception e) {
            e.printStackTrace();
            return context.getResources().getDimensionPixelSize(R.dimen.card_thumbnail_size);
        }
    }

    public int getTokenSplitGroupSize() {
        // the setting is of type "String", because ListPreference does not support integer arrays for its entryValues
        return  Integer.valueOf(
                getString(R.string.settings_key_split_group_size, R.string.settings_default_split_group_size)
        );
    }

    public Constants.TagFunctionality getTagFunctionality() {
        String tagFunctionality = getString(R.string.settings_key_tag_functionality, R.string.settings_default_tag_functionality);
        return Constants.TagFunctionality.valueOf(tagFunctionality.toUpperCase());
    }


    public boolean getScreenshotsEnabled() {
        return getBoolean(R.string.settings_key_enable_screenshot, false);
    }

    public boolean getUsedTokensDialogShown() {
        return getBoolean(R.string.settings_key_last_used_dialog_shown, false);
    }

    public void setUsedTokensDialogShown(boolean value) {
        setBoolean(R.string.settings_key_last_used_dialog_shown, value);
    }

    public boolean getNewBackupFormatDialogShown() {
        return getBoolean(R.string.settings_key_new_backup_format_dialog_shown, false);
    }

    public void setNewBackupFormatDialogShown(boolean value) {
        setBoolean(R.string.settings_key_new_backup_format_dialog_shown, value);
    }

    public boolean getAndroidBackupServiceEnabled() {
        return getBoolean(R.string.settings_key_enable_android_backup_service, true);
    }

    public void setAndroidBackupServiceEnabled(boolean value) {
        setBoolean(R.string.settings_key_enable_android_backup_service, value);
	}

    public boolean getIsAppendingDateTimeToBackups() {
        return getBoolean(R.string.settings_key_backup_append_date_time, true);
    }

    public int getAuthInactivityDelay() {
        return getIntValue(R.string.settings_key_auth_inactivity_delay, 0);
    }

    public boolean getAuthInactivity() {
        return getBoolean(R.string.settings_key_auth_inactivity, false);
    }
  
    public boolean isMinimizeAppOnCopyEnabled() {
        return  getBoolean(R.string.settings_key_minimize_on_copy, false);
    }
  
    private Constants.AutoBackup getAutoBackupEncryptedSetting() {
        String stringValue = getString(R.string.settings_key_auto_backup_password_enc, R.string.settings_default_auto_backup_password_enc);
        return Constants.AutoBackup.valueOf(stringValue.toUpperCase(Locale.ENGLISH));
    }

    public boolean getAutoBackupEncryptedPasswordsEnabled() {
        return getAutoBackupEncryptedSetting() != Constants.AutoBackup.OFF;
    }

    public boolean getAutoBackupEncryptedFullEnabled() {
        return getAutoBackupEncryptedSetting() == Constants.AutoBackup.ALL_EDITS;
    }

    public boolean isHighlightTokenOptionEnabled() {
        return getBoolean(R.string.settings_key_label_highlight_token,true);
    }

    public boolean isHideGlobalTimeoutEnabled() {
        return getBoolean(R.string.settings_key_hide_global_timeout, false);
    }

    public boolean isShowIndividualTimeoutsEnabled() {
        return getBoolean(R.string.settings_key_show_individual_timeouts, false);
    }

    public Constants.TapMode getTapSingle() {
        String singleTap = getString(R.string.settings_key_tap_single, R.string.settings_default_tap_single);
        return Constants.TapMode.valueOf(singleTap.toUpperCase(Locale.ENGLISH));
    }

    public Constants.TapMode getTapDouble() {
        String doubleTap = getString(R.string.settings_key_tap_double, R.string.settings_default_tap_double);
        return Constants.TapMode.valueOf(doubleTap.toUpperCase(Locale.ENGLISH));
    }

    public void setBackupLocation(Uri uri) {
        setString(R.string.settings_key_backup_location, uri.toString());
    }

    public Uri getBackupLocation() {
        return Uri.parse(getString(R.string.settings_key_backup_location, ""));
    }

    public boolean isBackupLocationSet() {
        return !getString(R.string.settings_key_backup_location, "").isEmpty();
    }
}
