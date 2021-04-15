/*
 * Copyright (C) 2019-2021 Jakob Nixdorf
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
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.shadowice.flocke.andotp.Utilities.Settings;

import java.util.Locale;

public class SecureCaptureActivity extends CaptureActivity {

    /**
     * Overwrites {@link CaptureActivity#initializeContent()} to:
     * <ul>
     *     <li>preventing the window from appearing in screenshots or from being viewed on
     *     non-secure displays, if this was enabled in the app settings.</li>
     *     <li>request hardware acceleration to be turned on.</li>
     *     <li>hide all screen decorations like navigation and status bars.</li>
     * </ul>
     *
     * <em>
     *     Note:
     *     {@link android.app.Activity#setContentView(int)} in {@link CaptureActivity#initializeContent()}
     *     of super class needs to be called before {@link Window#getInsetsController()}, otherwise NPE will be thrown
     *     because the top-level view of the current {@link Window}, containing the window decor, is not yet initialized.
     * </em>
     *
     * @return the DecoratedBarcodeView
     */
    @Override
    @SuppressWarnings("deprecation")
    protected DecoratedBarcodeView initializeContent() {
        Settings settings = new Settings(this);

        setTheme(settings.getTheme());
        setLocale(settings);

        //  This flag must be set before setting the content view of the activity or dialog.
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        // All window flags, that must be set before the window decoration is created, are already set
        // so we are safe to call super here.
        DecoratedBarcodeView barcodeScannerView = super.initializeContent();

        if (!settings.getScreenshotsEnabled()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        return barcodeScannerView;
    }

    private void setLocale(Settings settings) {
        Locale locale = settings.getLocale();
        Locale.setDefault(locale);

        Resources resources = getBaseContext().getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;

        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
