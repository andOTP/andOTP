package org.shadowice.flocke.andotp.Utilities;

import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public final class ConfirmedPasswordTransformationHelper {

    /** Sets up the specified password views for a toggleable obscure/view password text transformation. */
    public static void setup(TextInputLayout passwordLayout, TextInputEditText passwordInput, EditText passwordConfirmInput) {
        passwordLayout.setEndIconOnClickListener(v -> {
            boolean wasShowingPassword = passwordInput.getTransformationMethod() instanceof PasswordTransformationMethod;
            // Dispatch password visibility change to both password and confirm inputs
            dispatchPasswordVisibilityChange(passwordInput, wasShowingPassword);
            dispatchPasswordVisibilityChange(passwordConfirmInput, wasShowingPassword);
            passwordLayout.refreshDrawableState();
        });
        passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordConfirmInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    private static void dispatchPasswordVisibilityChange(EditText editText, boolean wasShowingPassword) {
        final int selection = editText.getSelectionEnd();
        PasswordTransformationMethod newMethod = wasShowingPassword ? null : PasswordTransformationMethod.getInstance();
        editText.setTransformationMethod(newMethod);
        if (selection >= 0) {
            editText.setSelection(selection);
        }
    }
}
