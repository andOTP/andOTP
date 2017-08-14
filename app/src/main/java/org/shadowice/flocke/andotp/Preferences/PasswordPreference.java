package org.shadowice.flocke.andotp.Preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.view.View;

import org.shadowice.flocke.andotp.R;

public class PasswordPreference extends DialogPreference {
    private static final String DEFAULT_VALUE = "";

    private TextInputEditText passwordInput;
    private String value = DEFAULT_VALUE;

    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.component_password);
    }

    @Override
    protected void onBindDialogView(View view) {
        passwordInput = (TextInputEditText) view.findViewById(R.id.passwordEdit);

        passwordInput.setText(value);

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
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            value = passwordInput.getText().toString();
            persistString(value);
        }
    }
}
