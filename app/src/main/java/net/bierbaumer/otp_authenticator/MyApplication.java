package net.bierbaumer.otp_authenticator;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PRNGFixes.apply();
    }
}
