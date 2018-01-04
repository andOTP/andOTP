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
import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.Preferences.CredentialsPreference;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.KeyStoreHelper;
import org.shadowice.flocke.andotp.Utilities.Settings;

import java.util.ArrayList;

import javax.crypto.SecretKey;

import static org.shadowice.flocke.andotp.Utilities.Constants.AuthMethod;
import static org.shadowice.flocke.andotp.Utilities.Constants.EncryptionType;

public class SettingsActivity extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    SettingsFragment fragment;

    SecretKey encryptionKey = null;
    boolean encryptionChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.settings_activity_title);
        setContentView(R.layout.activity_container);

        Toolbar toolbar = findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);

        ViewStub stub = findViewById(R.id.container_stub);
        stub.inflate();

        Intent callingIntent = getIntent();
        byte[] keyMaterial = callingIntent.getByteArrayExtra(Constants.EXTRA_SETTINGS_ENCRYPTION_KEY);
        if (keyMaterial != null && keyMaterial.length > 0)
            encryptionKey = EncryptionHelper.generateSymmetricKey(keyMaterial);

        fragment = new SettingsFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.container_content, fragment)
                .commit();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    public void finishWithResult() {
        Intent data = new Intent();

        data.putExtra(Constants.EXTRA_SETTINGS_ENCRYPTION_CHANGED, encryptionChanged);
        data.putExtra(Constants.EXTRA_SETTINGS_ENCRYPTION_KEY, encryptionKey.getEncoded());

        setResult(RESULT_OK, data);
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

    private void tryEncryptionChangeWithAuth(EncryptionType newEnc) {
        Intent authIntent = new Intent(this, AuthenticateActivity.class);
        authIntent.putExtra(Constants.EXTRA_AUTH_NEW_ENCRYPTION, newEnc.name());
        authIntent.putExtra(Constants.EXTRA_AUTH_MESSAGE, R.string.auth_msg_confirm_encryption);
        startActivityForResult(authIntent, Constants.INTENT_SETTINGS_AUTHENTICATE);
    }

    private boolean tryEncryptionChange(EncryptionType newEnc, byte[] newKey) {
        Toast upgrading = Toast.makeText(this, R.string.settings_toast_encryption_changing, Toast.LENGTH_LONG);
        upgrading.show();

        if (DatabaseHelper.backupDatabase(this)) {
            ArrayList<Entry> entries;

            if (encryptionKey != null)
                entries = DatabaseHelper.loadDatabase(this, encryptionKey);
            else
                entries = new ArrayList<>();

            SecretKey newEncryptionKey;

            if (newEnc == EncryptionType.KEYSTORE) {
                newEncryptionKey = KeyStoreHelper.loadEncryptionKeyFromKeyStore(this, true);
            } else if (newKey != null && newKey.length > 0) {
                newEncryptionKey = EncryptionHelper.generateSymmetricKey(newKey);
            } else {
                upgrading.cancel();
                DatabaseHelper.restoreDatabaseBackup(this);
                return false;
            }

            if (DatabaseHelper.saveDatabase(this, entries, newEncryptionKey)) {
                encryptionKey = newEncryptionKey;
                encryptionChanged = true;

                fragment.encryption.setValue(newEnc.name().toLowerCase());

                upgrading.cancel();
                Toast.makeText(this, R.string.settings_toast_encryption_change_success, Toast.LENGTH_LONG).show();

                return true;
            }

            DatabaseHelper.restoreDatabaseBackup(this);

            upgrading.cancel();
            Toast.makeText(this, R.string.settings_toast_encryption_change_failed, Toast.LENGTH_LONG).show();
        } else {
            upgrading.cancel();
            Toast.makeText(this, R.string.settings_toast_encryption_backup_failed, Toast.LENGTH_LONG).show();
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.INTENT_SETTINGS_AUTHENTICATE) {
            if (resultCode == RESULT_OK) {
                byte[] authKey = data.getByteArrayExtra(Constants.EXTRA_AUTH_PASSWORD_KEY);
                String newEnc = data.getStringExtra(Constants.EXTRA_AUTH_NEW_ENCRYPTION);

                if (authKey != null && authKey.length > 0 && newEnc != null && !newEnc.isEmpty()) {
                    EncryptionType newEncType = EncryptionType.valueOf(newEnc);
                    tryEncryptionChange(newEncType, authKey);
                } else {
                    Toast.makeText(this, R.string.settings_toast_encryption_no_key, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, R.string.settings_toast_encryption_auth_failed, Toast.LENGTH_LONG).show();
            }
        } else if (fragment.pgpKey.handleOnActivityResult(requestCode, resultCode, data)) {
            // handled by OpenPgpKeyPreference
            return;
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        PreferenceCategory catSecurity;

        Settings settings;
        ListPreference encryption;

        OpenPgpAppPreference pgpProvider;
        OpenPgpKeyPreference pgpKey;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            settings = new Settings(getActivity());

            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
            addPreferencesFromResource(R.xml.preferences);

            CredentialsPreference credentialsPreference = (CredentialsPreference) findPreference(getString(R.string.settings_key_auth));
            credentialsPreference.setEncryptionChangeCallback(new CredentialsPreference.EncryptionChangeCallback() {
                @Override
                public boolean testEncryptionChange(byte[] newKey) {
                    return ((SettingsActivity) getActivity()).tryEncryptionChange(settings.getEncryption(), newKey);
                }
            });

            // Authentication
            catSecurity = (PreferenceCategory) findPreference(getString(R.string.settings_key_cat_security));
            encryption = (ListPreference) findPreference(getString(R.string.settings_key_encryption));

            encryption.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, Object o) {
                    String newEncryption = (String) o;
                    EncryptionType encryptionType = EncryptionType.valueOf(newEncryption.toUpperCase());
                    AuthMethod authMethod = settings.getAuthMethod();

                    if (encryptionType == EncryptionType.PASSWORD) {
                        if (authMethod != AuthMethod.PASSWORD && authMethod != AuthMethod.PIN) {
                            Toast.makeText(getActivity(), R.string.settings_toast_encryption_invalid_with_auth, Toast.LENGTH_LONG).show();
                            return false;
                        } else {
                            if (settings.getAuthCredentials(authMethod).isEmpty()) {
                                Toast.makeText(getActivity(), R.string.settings_toast_encryption_invalid_without_credentials, Toast.LENGTH_LONG).show();
                                return false;
                            }
                        }

                        ((SettingsActivity) getActivity()).tryEncryptionChangeWithAuth(encryptionType);
                    } else if (encryptionType == EncryptionType.KEYSTORE) {
                        ((SettingsActivity) getActivity()).tryEncryptionChange(encryptionType, null);
                    }

                    return false;
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
