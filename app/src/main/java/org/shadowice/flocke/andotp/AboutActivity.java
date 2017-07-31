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

package org.shadowice.flocke.andotp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.psdev.licensesdialog.LicensesDialog;

public class AboutActivity extends AppCompatActivity {
    private static final String GITHUB_URI = "https://github.com/flocke/andOTP";
    private static final String CHANGELOG_URI = GITHUB_URI + "/blob/master/CHANGELOG.md";
    private static final String MIT_URI = GITHUB_URI + "/blob/master/LICENSE.txt";

    private static final String AUTHOR1_GITHUB = "https://github.com/flocke";
    private static final String AUTHOR1_PAYPAL = "https://paypal.me/flocke000";

    private static final String AUTHOR2_GITHUB = "https://github.com/0xbb";
    private static final String AUTHOR2_APP = AUTHOR2_GITHUB + "/otp-authenticator";

    private static final String BUGREPORT_URI = GITHUB_URI + "/issues";

    private void setThemeFromPrefs() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPref.getString(getString(R.string.settings_key_theme), getString(R.string.settings_default_theme));

        if (theme.equals("light")) {
            setTheme(R.style.AppTheme_NoActionBar);
        } else if (theme.equals("dark")) {
            setTheme(R.style.AppTheme_Dark_NoActionBar);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setThemeFromPrefs();

        super.onCreate(savedInstanceState);

        setTitle(R.string.about_activity_title);
        setContentView(R.layout.activity_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);

        ViewStub stub = (ViewStub) findViewById(R.id.container_stub);
        stub.setLayoutResource(R.layout.content_about);
        View v = stub.inflate();

        String versionName = "";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView version = (TextView) v.findViewById(R.id.about_text_version);
        version.setText(versionName);

        LinearLayout license = (LinearLayout) v.findViewById(R.id.about_layout_license);
        license.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(MIT_URI);
            }
        });

        LinearLayout changelog = (LinearLayout) v.findViewById(R.id.about_layout_changelog);
        changelog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(CHANGELOG_URI);
            }
        });

        LinearLayout source = (LinearLayout) v.findViewById(R.id.about_layout_source);
        source.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(GITHUB_URI);
            }
        });

        LinearLayout licenses = (LinearLayout) v.findViewById(R.id.about_layout_licenses);
        licenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLicenses();
            }
        });

        TextView author1GitHub = (TextView) v.findViewById(R.id.about_author1_github);
        TextView author1Paypal = (TextView) v.findViewById(R.id.about_author1_paypal);

        author1GitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(AUTHOR1_GITHUB);
            }
        });
        author1Paypal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(AUTHOR1_PAYPAL);
            }
        });

        TextView author2GitHub = (TextView) v.findViewById(R.id.about_author2_github);
        TextView author2App = (TextView) v.findViewById(R.id.about_author2_app);

        author2GitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(AUTHOR2_GITHUB);
            }
        });

        author2App.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(AUTHOR2_APP);
            }
        });

        LinearLayout bugReport = (LinearLayout) v.findViewById(R.id.about_layout_bugs);
        bugReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openURI(BUGREPORT_URI);
            }
        });

    }

    // Go back to the main activity
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public void openURI(String uri) {
        Intent openURI = new Intent(Intent.ACTION_VIEW);
        openURI.setData(Uri.parse(uri));
        startActivity(openURI);
    }

    public void showLicenses() {
        new LicensesDialog.Builder(this)
                .setNotices(R.raw.licenses)
                .setIncludeOwnLicense(true)
                .setTitle(R.string.about_label_licenses)
                .build()
                .show();
    }
}
