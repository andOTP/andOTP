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

package org.shadowice.flocke.andotp.Activities;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.Tools;

import java.util.Locale;

public abstract class ThemedActivity extends AppCompatActivity {
    public Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = new Settings(this);

        setTheme(settings.getTheme());
        setLocale();

        //Set navigation bar color
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Tools.getThemeColor(this,R.attr.navigationBarColor));
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        setLocale();

        super.onResume();
    }

    public void setLocale() {
        Locale locale = settings.getLocale();
        Locale.setDefault(locale);

        Resources resources = getBaseContext().getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;

        // TODO: updateConfiguration is marked as deprecated. Replace with android.content.Context.createConfigurationContext
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
