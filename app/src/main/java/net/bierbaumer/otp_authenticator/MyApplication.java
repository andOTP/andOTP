package net.bierbaumer.otp_authenticator;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        PRNGFixes.apply();
        Log.e("AAA", "AAAA");
    }
}
