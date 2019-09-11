package org.shadowice.flocke.andotp.Activities;

import android.view.WindowManager;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import org.shadowice.flocke.andotp.Utilities.Settings;

public class SecureCaptureActivity extends CaptureActivity {
    @Override
    protected DecoratedBarcodeView initializeContent() {
        Settings settings = new Settings(this);
        if (!settings.getScreenshotsEnabled())
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        return super.initializeContent();
    }
}
