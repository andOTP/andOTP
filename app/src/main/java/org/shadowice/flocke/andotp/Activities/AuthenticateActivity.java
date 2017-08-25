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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.shadowice.flocke.andotp.R;

import static org.shadowice.flocke.andotp.Utilities.Settings.AuthMethod;

public class AuthenticateActivity extends ThemedActivity
    implements EditText.OnEditorActionListener {
    private String password;

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

        AuthMethod authMethod = settings.getAuthMethod();

        if (authMethod == AuthMethod.PASSWORD) {
            password = settings.getAuthPassword();

            if (password.isEmpty()) {
                Toast.makeText(this, R.string.auth_toast_password_missing, Toast.LENGTH_LONG).show();
                finishWithResult(true);
            } else {
                passwordLabel.setText(R.string.auth_msg_password);
                passwordLayout.setHint(getString(R.string.auth_hint_password));
                passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        } else if (authMethod == AuthMethod.PIN) {
            password = settings.getAuthPIN();

            if (password.isEmpty()) {
                Toast.makeText(this, R.string.auth_toast_pin_missing, Toast.LENGTH_LONG).show();
                finishWithResult(true);
            } else {
                passwordLabel.setText(R.string.auth_msg_pin);
                passwordLayout.setHint(getString(R.string.auth_hint_pin));
                passwordInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            }
        } else {
            finishWithResult(true);
        }

        passwordInput.setTransformationMethod(new PasswordTransformationMethod());
        passwordInput.setOnEditorActionListener(this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            String input = v.getText().toString();

            if (input.equals(password)) {
                finishWithResult(true);
            } else {
                finishWithResult(false);
            }

            return true;
        }

        return false;
    }

    // End with a result
    public void finishWithResult(boolean success) {
        Intent data = new Intent();

        if (success)
            setResult(RESULT_OK, data);

        finish();
    }

    // Go back to the main activity
    @Override
    public void onBackPressed() {
        finishWithResult(false);
        super.onBackPressed();
    }
}
