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

import android.app.KeyguardManager;
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
import org.shadowice.flocke.andotp.Preferences.PBKDF2PasswordPreference;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.KeyStoreHelper;
import org.shadowice.flocke.andotp.Utilities.Settings;

public class SettingsActivity extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{
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

    public void finishWithResult() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finishWithResult();
        return true;
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
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

        public void updateAuthPassword(Settings.AuthMethod newAuth) {
            PBKDF2PasswordPreference pwPref = (PBKDF2PasswordPreference) catSecurity.findPreference(getString(R.string.settings_key_auth_password_pbkdf2));
            PBKDF2PasswordPreference pinPref = (PBKDF2PasswordPreference) catSecurity.findPreference(getString(R.string.settings_key_auth_pin_pbkdf2));

            if (pwPref != null)
                catSecurity.removePreference(pwPref);
            if (pinPref != null)
                catSecurity.removePreference(pinPref);

            if (newAuth == Settings.AuthMethod.PASSWORD) {
                PBKDF2PasswordPreference authPassword = new PBKDF2PasswordPreference(getActivity(), null);
                authPassword.setTitle(R.string.settings_title_auth_password);
                authPassword.setOrder(4);
                authPassword.setKey(getString(R.string.settings_key_auth_password_pbkdf2));
                authPassword.setMode(PBKDF2PasswordPreference.Mode.PASSWORD);

                catSecurity.addPreference(authPassword);
            } else if (newAuth == Settings.AuthMethod.PIN) {
                PBKDF2PasswordPreference authPIN = new PBKDF2PasswordPreference(getActivity(), null);
                authPIN.setTitle(R.string.settings_title_auth_pin);
                authPIN.setOrder(4);
                authPIN.setKey(getString(R.string.settings_key_auth_pin_pbkdf2));
                authPIN.setMode(PBKDF2PasswordPreference.Mode.PIN);

                catSecurity.addPreference(authPIN);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

            addPreferencesFromResource(R.xml.preferences);

            // Authentication
            catSecurity = (PreferenceCategory) findPreference(getString(R.string.settings_key_cat_security));
            ListPreference authPref = (ListPreference) findPreference(getString(R.string.settings_key_auth));
            encryption = (ListPreference) findPreference(getString(R.string.settings_key_encryption));

            updateAuthPassword(Settings.AuthMethod.valueOf(authPref.getValue().toUpperCase()));

            authPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    String newAuth = (String) o;
                    String encryption = sharedPref.getString(getString(R.string.settings_key_encryption), getString(R.string.settings_default_encryption));

                    Constants.EncryptionType encryptionType = Constants.EncryptionType.valueOf(encryption.toUpperCase());
                    Settings.AuthMethod authMethod = Settings.AuthMethod.valueOf(newAuth.toUpperCase());

                    if (encryptionType == Constants.EncryptionType.PASSWORD) {
                        if (authMethod == Settings.AuthMethod.NONE || authMethod == Settings.AuthMethod.DEVICE) {
                            Toast.makeText(getActivity(), R.string.settings_toast_auth_invalid_with_encryption, Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }

                    if (authMethod == Settings.AuthMethod.DEVICE) {
                        KeyguardManager km = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);

                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                            Toast.makeText(getActivity(), R.string.settings_toast_auth_device_pre_lollipop, Toast.LENGTH_LONG).show();
                            return false;
                        } else if (! km.isKeyguardSecure()) {
                            Toast.makeText(getActivity(), R.string.settings_toast_auth_device_not_secure, Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }

                    updateAuthPassword(authMethod);

                    return true;
                }
            });

            encryption.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, Object o) {
                    String newEncryption = (String) o;
                    String auth = sharedPref.getString(getString(R.string.settings_key_auth), getString(R.string.settings_default_auth));
                    Constants.EncryptionType encryptionType = Constants.EncryptionType.valueOf(newEncryption.toUpperCase());
                    Settings.AuthMethod authMethod = Settings.AuthMethod.valueOf(auth.toUpperCase());

                    if (encryptionType == Constants.EncryptionType.PASSWORD) {
                        if (authMethod != Settings.AuthMethod.PASSWORD && authMethod != Settings.AuthMethod.PIN) {
                            Toast.makeText(getActivity(), R.string.settings_toast_encryption_invalid_with_auth, Toast.LENGTH_LONG).show();
                            return false;
                        } else {
                            String credentials = "";
                            if (authMethod == Settings.AuthMethod.PASSWORD)
                                credentials = sharedPref.getString(getString(R.string.settings_key_auth_password_pbkdf2), "");
                            else if (authMethod == Settings.AuthMethod.PIN)
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
