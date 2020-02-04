package org.shadowice.flocke.andotp.Activities;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.WindowManager;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.shadowice.flocke.andotp.Utilities.Settings;

import java.util.Locale;

public class SecureCaptureActivity extends CaptureActivity {
    @Override
    protected DecoratedBarcodeView initializeContent() {
        Settings settings = new Settings(this);

        setTheme(settings.getTheme());
        setLocale(settings);

        if (!settings.getScreenshotsEnabled())
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        return super.initializeContent();
    }

    private void setLocale(Settings settings) {
        Locale locale = settings.getLocale();
        Locale.setDefault(locale);

        Resources resources = getBaseContext().getResources();
        Configuration config = resources.getConfiguration();
        config.locale = locale;

        // TODO: updateConfiguration is marked as deprecated. Replace with android.content.Context.createConfigurationContext
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
