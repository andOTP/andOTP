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

package org.shadowice.flocke.andotp.Preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.design.widget.TextInputEditText;
import android.app.AlertDialog;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.shadowice.flocke.andotp.R;

public class PasswordPreference extends DialogPreference
    implements View.OnClickListener, TextWatcher {

    public enum Mode {
        PASSWORD, PIN
    }

    private static final String DEFAULT_VALUE = "";

    private Mode mode = Mode.PASSWORD;

    private TextInputEditText passwordInput;
    private EditText passwordConfirm;

    private Button btnSave;

    private String value = DEFAULT_VALUE;

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.component_password);
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        TextInputLayout passwordLayout = (TextInputLayout) view.findViewById(R.id.passwordLayout);
        passwordInput = (TextInputEditText) view.findViewById(R.id.passwordEdit);
        passwordConfirm = (EditText) view.findViewById(R.id.passwordConfirm);

        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
        btnSave = (Button) view.findViewById(R.id.btnSave);
        btnSave.setEnabled(false);

        btnCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        if (! value.isEmpty()) {
            passwordInput.setText(value);
        }

        if (mode == Mode.PASSWORD) {
            passwordLayout.setHint(getContext().getString(R.string.settings_hint_password));
            passwordConfirm.setHint(R.string.settings_hint_password_confirm);

            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordInput.setTransformationMethod(new PasswordTransformationMethod());
            passwordConfirm.setTransformationMethod(new PasswordTransformationMethod());
        } else if (mode == Mode.PIN) {
            passwordLayout.setHint(getContext().getString(R.string.settings_hint_pin));
            passwordConfirm.setHint(R.string.settings_hint_pin_confirm);

            passwordInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            passwordConfirm.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            passwordInput.setTransformationMethod(new PasswordTransformationMethod());
            passwordConfirm.setTransformationMethod(new PasswordTransformationMethod());
        }

        passwordConfirm.addTextChangedListener(this);
        passwordInput.addTextChangedListener(this);

        super.onBindDialogView(view);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            value = getPersistedString(DEFAULT_VALUE);
        } else {
            value = (String) defaultValue;
            persistString(value);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.btnCancel):
                getDialog().dismiss();
                break;
            case (R.id.btnSave):
                value = passwordInput.getText().toString();
                persistString(value);

                getDialog().dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (passwordConfirm.getEditableText().toString().equals(passwordInput.getEditableText().toString())) {
            btnSave.setEnabled(true);
        } else {
            btnSave.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
}
