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

package org.shadowice.flocke.andotp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static org.shadowice.flocke.andotp.Utilities.Settings.AuthMethod;

public class AuthenticateActivity extends ThemedActivity
    implements EditText.OnEditorActionListener {
    public static final String EXTRA_NAME_SEED = "credential_seed";

    private String password;

    AuthMethod authMethod;
    boolean oldPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.auth_activity_title);
        setContentView(R.layout.activity_container);

        Toolbar toolbar = findViewById(R.id.container_toolbar);
        toolbar.setNavigationIcon(null);
        setSupportActionBar(toolbar);

        ViewStub stub = findViewById(R.id.container_stub);
        stub.setLayoutResource(R.layout.content_authenticate);
        View v = stub.inflate();

        TextView passwordLabel = v.findViewById(R.id.passwordLabel);
        TextInputLayout passwordLayout = v.findViewById(R.id.passwordLayout);
        TextInputEditText passwordInput = v.findViewById(R.id.passwordEdit);

        authMethod = settings.getAuthMethod();

        if (authMethod == AuthMethod.PASSWORD) {
            password = settings.getAuthPasswordPBKDF2();

            if (password.isEmpty()) {
                password = settings.getAuthPasswordHash();
                oldPassword = true;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, R.string.auth_toast_password_missing, Toast.LENGTH_LONG).show();
                finishWithResult(true, null);
            } else {
                passwordLabel.setText(R.string.auth_msg_password);
                passwordLayout.setHint(getString(R.string.auth_hint_password));
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        } else if (authMethod == AuthMethod.PIN) {
            password = settings.getAuthPINPBKDF2();

            if (password.isEmpty()) {
                password = settings.getAuthPINHash();
                oldPassword = true;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, R.string.auth_toast_pin_missing, Toast.LENGTH_LONG).show();
                finishWithResult(true, null);
            } else {
                passwordLabel.setText(R.string.auth_msg_pin);
                passwordLayout.setHint(getString(R.string.auth_hint_pin));
                passwordInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            }
        } else {
            finishWithResult(true, null);
        }

        passwordInput.setTransformationMethod(new PasswordTransformationMethod());
        passwordInput.setOnEditorActionListener(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (! oldPassword) {
                try {
                    EncryptionHelper.PBKDF2Credentials credentials = EncryptionHelper.generatePBKDF2Credentials(v.getText().toString(), settings.getSalt(), settings.getIterations(authMethod));
                    byte[] passwordArray = Base64.decode(password, Base64.URL_SAFE);

                    if (Arrays.equals(passwordArray, credentials.password)) {
                        finishWithResult(true, credentials.seed);
                    } else {
                        finishWithResult(false, null);
                    }
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    e.printStackTrace();
                    finishWithResult(false, null);
                }
            } else {
                String plainPassword = v.getText().toString();
                String hashedPassword = new String(Hex.encodeHex(DigestUtils.sha256(plainPassword)));

                if (hashedPassword.equals(password)) {
                    byte[] seed = null;

                    try {
                        int iter = EncryptionHelper.generateRandomIterations();
                        EncryptionHelper.PBKDF2Credentials credentials = EncryptionHelper.generatePBKDF2Credentials(plainPassword, settings.getSalt(), iter);
                        String base64 = Base64.encodeToString(credentials.password, Base64.URL_SAFE);

                        if (authMethod == AuthMethod.PASSWORD)
                            settings.setAuthPasswordPBKDF2(base64);
                        else if (authMethod == AuthMethod.PIN)
                            settings.setAuthPINPBKDF2(base64);

                        settings.setIterations(authMethod, iter);

                        seed = credentials.seed;
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                        Toast.makeText(this, R.string.settings_toast_auth_upgrade_failed, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    if (authMethod == AuthMethod.PASSWORD)
                        settings.removeAuthPasswordHash();
                    else if (authMethod == AuthMethod.PIN)
                        settings.removeAuthPINHash();

                    finishWithResult(true, seed);
                } else {
                    finishWithResult(false, null);
                }
            }

            return true;
        }

        return false;
    }

    // End with a result
    public void finishWithResult(boolean success, byte[] seed) {
        Intent data = new Intent();

        if (seed != null)
            data.putExtra(EXTRA_NAME_SEED, seed);

        if (success)
            setResult(RESULT_OK, data);

        finish();
    }

    // Go back to the main activity
    @Override
    public void onBackPressed() {
        finishWithResult(false, null);
        super.onBackPressed();
    }
}
