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

package org.shadowice.flocke.andotp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.ViewStub;
import android.widget.Toast;

import org.openintents.openpgp.util.OpenPgpAppPreference;
import org.openintents.openpgp.util.OpenPgpKeyPreference;
import org.shadowice.flocke.andotp.Preferences.CredentialsPreference;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.KeyStoreHelper;

import static org.shadowice.flocke.andotp.Utilities.Constants.AuthMethod;
import static org.shadowice.flocke.andotp.Utilities.Constants.EncryptionType;

public class SettingsActivity extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    public static final String SETTINGS_EXTRA_NAME_ENCRYPTION_CHANGED = "encryption_changed";
    public static final String SETTINGS_EXTRA_NAME_ENCRYPTION_KEY = "encryption_key";

    SettingsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.settings_activity_title);
        setContentView(R.layout.activity_container);

        Toolbar toolbar = findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);

        ViewStub stub = findViewById(R.id.container_stub);
        stub.inflate();

        fragment = new SettingsFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.container_content, fragment)
                .commit();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    public void finishWithResult(boolean encryptionChanged, byte[] newKey) {
        Intent data = new Intent();

        data.putExtra(SETTINGS_EXTRA_NAME_ENCRYPTION_CHANGED, encryptionChanged);

        if (newKey != null)
            data.putExtra(SETTINGS_EXTRA_NAME_ENCRYPTION_KEY, newKey);

        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finishWithResult(false,null);
        return true;
    }

    @Override
    public void onBackPressed() {
        finishWithResult(false, null);
        super.onBackPressed();
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_key_theme)) ||
                key.equals(getString(R.string.settings_key_lang)) ||
                key.equals(getString(R.string.settings_key_special_features))) {
            recreate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (fragment.pgpKey.handleOnActivityResult(requestCode, resultCode, data)) {
            // handled by OpenPgpKeyPreference
            return;
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        PreferenceCategory catSecurity;

        ListPreference encryption;

        OpenPgpAppPreference pgpProvider;
        OpenPgpKeyPreference pgpKey;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
            addPreferencesFromResource(R.xml.preferences);

            CredentialsPreference credentialsPreference = (CredentialsPreference) findPreference(getString(R.string.settings_key_auth));
            credentialsPreference.setEncryptionChangeHandler(new CredentialsPreference.EncryptionChangeHandler() {
                @Override
                public void onEncryptionChanged(byte[] newKey) {
                    ((SettingsActivity) getActivity()).finishWithResult(true, newKey);
                }
            });

            // Authentication
            catSecurity = (PreferenceCategory) findPreference(getString(R.string.settings_key_cat_security));
            encryption = (ListPreference) findPreference(getString(R.string.settings_key_encryption));

            encryption.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, Object o) {
                    String newEncryption = (String) o;
                    String auth = sharedPref.getString(getString(R.string.settings_key_auth), CredentialsPreference.DEFAULT_VALUE.name().toLowerCase());
                    EncryptionType encryptionType = EncryptionType.valueOf(newEncryption.toUpperCase());
                    AuthMethod authMethod = AuthMethod.valueOf(auth.toUpperCase());

                    if (encryptionType == EncryptionType.PASSWORD) {
                        if (authMethod != AuthMethod.PASSWORD && authMethod != AuthMethod.PIN) {
                            Toast.makeText(getActivity(), R.string.settings_toast_encryption_invalid_with_auth, Toast.LENGTH_LONG).show();
                            return false;
                        } else {
                            String credentials = "";
                            if (authMethod == AuthMethod.PASSWORD)
                                credentials = sharedPref.getString(getString(R.string.settings_key_auth_password_pbkdf2), "");
                            else if (authMethod == AuthMethod.PIN)
                                credentials = sharedPref.getString(getString(R.string.settings_key_auth_pin_pbkdf2), "");

                            if (credentials.isEmpty()) {
                                Toast.makeText(getActivity(), R.string.settings_toast_encryption_invalid_without_credentials, Toast.LENGTH_LONG).show();
                                return false;
                            } else {
                                KeyStoreHelper.wipeKeys(preference.getContext());
                            }
                        }
                    }

                    encryption.setValue(newEncryption);
                    ((SettingsActivity) getActivity()).finishWithResult(true, null);

                    return true;
                }
            });

            // OpenPGP
            pgpProvider = (OpenPgpAppPreference) findPreference(getString(R.string.settings_key_openpgp_provider));
            pgpKey = (OpenPgpKeyPreference) findPreference(getString(R.string.settings_key_openpgp_keyid));

            pgpKey.setOpenPgpProvider(pgpProvider.getValue());
            pgpProvider.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    pgpKey.setOpenPgpProvider((String) newValue);
                    return true;
                }
            });
            pgpKey.setDefaultUserId("Alice <alice@example.com>");


            if (sharedPref.contains(getString(R.string.settings_key_special_features)) &&
                    sharedPref.getBoolean(getString(R.string.settings_key_special_features), false)) {
                addPreferencesFromResource(R.xml.preferences_special);
            }
        }
    }
}
