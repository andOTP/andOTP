package org.shadowice.flocke.andotp;

import android.app.KeyguardManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewStub;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.settings_activity_title);
        setContentView(R.layout.activity_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);

        ViewStub stub = (ViewStub) findViewById(R.id.container_stub);
        stub.inflate();

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

            Preference authPref = findPreference(getString(R.string.settings_key_auth_device));
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                authPref.setSummary(R.string.settings_desc_auth_device_pre_lollipop);
                authPref.setEnabled(false);
                authPref.setSelectable(false);
            } else {
                KeyguardManager km = (KeyguardManager) getActivity().getSystemService(KEYGUARD_SERVICE);
                if (! km.isKeyguardSecure()) {
                    authPref.setSummary(R.string.settings_desc_auth_device_not_secure);
                    authPref.setEnabled(false);
                    authPref.setSelectable(false);
                }
            }
        }
    }
}
