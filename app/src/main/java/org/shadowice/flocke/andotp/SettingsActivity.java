package org.shadowice.flocke.andotp;

import android.app.KeyguardManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.activity_title_settings);
        setContentView(R.layout.activity_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction()
                .replace(R.id.container_content, new SettingsFragment())
                .commit();
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

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            Preference authPref = findPreference(getString(R.string.pref_key_auth_device));
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                authPref.setSummary(R.string.pref_desc_auth_device_pre_lollipop);
                authPref.setEnabled(false);
                authPref.setSelectable(false);
            } else {
                KeyguardManager km = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);
                if (! km.isKeyguardSecure()) {
                    authPref.setSummary(R.string.pref_desc_auth_device_not_secure);
                    authPref.setEnabled(false);
                    authPref.setSelectable(false);
                }
            }
        }
    }
}
