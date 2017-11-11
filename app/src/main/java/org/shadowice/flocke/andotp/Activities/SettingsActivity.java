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
import org.shadowice.flocke.andotp.Preferences.PasswordHashPreference;
import org.shadowice.flocke.andotp.R;
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
        if (key.equals(getString(R.string.settings_key_theme))) {
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

        OpenPgpAppPreference pgpProvider;
        OpenPgpKeyPreference pgpKey;

        public void updateAuthPassword(String newAuth) {
            PasswordHashPreference pwPref = (PasswordHashPreference) catSecurity.findPreference(getString(R.string.settings_key_auth_password_hash));
            PasswordHashPreference pinPref = (PasswordHashPreference) catSecurity.findPreference(getString(R.string.settings_key_auth_pin_hash));

            if (pwPref != null)
                catSecurity.removePreference(pwPref);
            if (pinPref != null)
                catSecurity.removePreference(pinPref);

            switch (newAuth) {
                case "password":
                    PasswordHashPreference authPassword = new PasswordHashPreference(getActivity(), null);
                    authPassword.setTitle(R.string.settings_title_auth_password);
                    authPassword.setOrder(3);
                    authPassword.setKey(getString(R.string.settings_key_auth_password_hash));
                    authPassword.setMode(PasswordHashPreference.Mode.PASSWORD);

                    catSecurity.addPreference(authPassword);

                    break;

                case "pin":
                    PasswordHashPreference authPIN = new PasswordHashPreference(getActivity(), null);
                    authPIN.setTitle(R.string.settings_title_auth_pin);
                    authPIN.setOrder(3);
                    authPIN.setKey(getString(R.string.settings_key_auth_pin_hash));
                    authPIN.setMode(PasswordHashPreference.Mode.PIN);

                    catSecurity.addPreference(authPIN);

                    break;

                default:
                    break;
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            Settings settings = new Settings(getActivity());
            if(settings.getSpecialFeatures()) {
                addPreferencesFromResource(R.xml.preferences_special_features);
            }

            // Authentication
            catSecurity = (PreferenceCategory) findPreference(getString(R.string.settings_key_cat_security));
            ListPreference authPref = (ListPreference) findPreference(getString(R.string.settings_key_auth));

            updateAuthPassword(authPref.getValue());

            authPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    String newAuth = (String) o;

                    if (newAuth.equals("device")) {
                        KeyguardManager km = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);

                        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                            Toast.makeText(getActivity(), R.string.settings_toast_auth_device_pre_lollipop, Toast.LENGTH_LONG).show();
                            return false;
                        } else if (! km.isKeyguardSecure()) {
                            Toast.makeText(getActivity(), R.string.settings_toast_auth_device_not_secure, Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }

                    updateAuthPassword(newAuth);

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
        }
    }
}
