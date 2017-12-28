/*
 * Copyright (C) 2017 Carlos Melero
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

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import org.shadowice.flocke.andotp.Utilities.Settings;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Set;

import static org.shadowice.flocke.andotp.Utilities.DatabaseHelper.SETTINGS_FILE;
import static org.shadowice.flocke.andotp.Utilities.KeyStoreHelper.KEYSTORE_ALIAS_WRAPPING;
import static org.shadowice.flocke.andotp.Utilities.KeyStoreHelper.KEY_FILE;

public class PanicResponderActivity extends Activity {
    public static final String PANIC_TRIGGER_ACTION = "info.guardianproject.panic.action.TRIGGER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null && PANIC_TRIGGER_ACTION.equals(intent.getAction())) {
            Settings settings = new Settings(this);

            Set<String> response = settings.getPanicResponse();

            if (response.contains("accounts")) {
                File database = new File(getFilesDir() + "/" + SETTINGS_FILE);
                File key = new File(getFilesDir() + "/" + KEY_FILE);

                database.delete();
                key.delete();

                try {
                    final KeyStore keyStore = java.security.KeyStore.getInstance("AndroidKeyStore");
                    keyStore.load(null);

                    if (keyStore.containsAlias(KEYSTORE_ALIAS_WRAPPING)) {
                        keyStore.deleteEntry(KEYSTORE_ALIAS_WRAPPING);
                    }
                } catch (GeneralSecurityException | IOException e) {
                    e.printStackTrace();
                }
            }

            if (response.contains("settings"))
                settings.clear(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
    }
}