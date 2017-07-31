package org.shadowice.flocke.andotp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPref.getString(getString(R.string.settings_key_theme), getString(R.string.settings_default_theme));

        if (theme.equals("light")) {
            setTheme(R.style.AppTheme_NoActionBar);
        } else if (theme.equals("dark")) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }

        super.onCreate(savedInstanceState);
    }
}
