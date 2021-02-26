package org.shadowice.flocke.andotp.Dialogs;

import android.content.Context;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatDialog;

import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.ConfirmedPasswordTransformationHelper;
import org.shadowice.flocke.andotp.Utilities.EditorActionHelper;
import org.shadowice.flocke.andotp.Utilities.Tools;

public class PasswordEntryDialog extends AppCompatDialog
    implements View.OnClickListener, TextWatcher, TextView.OnEditorActionListener {

    public enum Mode { ENTER, UPDATE }

    public interface PasswordEnteredCallback {
        void onPasswordEntered(String newPassword);
    }

    private Mode dialogMode;
    private PasswordEnteredCallback callback;

    private TextInputEditText passwordInput;
    private EditText passwordConfirm;
    private Button okButton;

    public PasswordEntryDialog(Context context, Mode newMode, boolean blockAccessibility, boolean blockAutofill, PasswordEnteredCallback newCallback) {
        super(context, Tools.getThemeResource(context, R.attr.dialogTheme));

        setTitle(R.string.dialog_title_enter_password);
        setContentView(R.layout.dialog_password_entry);

        TextInputLayout passwordLayout = findViewById(R.id.passwordInputLayout);
        passwordInput = findViewById(R.id.passwordInput);
        passwordConfirm = findViewById(R.id.passwordConfirm);
        ConfirmedPasswordTransformationHelper.setup(passwordLayout, passwordInput, passwordConfirm);

        if (blockAccessibility) {
            passwordLayout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            passwordConfirm.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && blockAutofill) {
            passwordLayout.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
            passwordConfirm.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }

        okButton = findViewById(R.id.buttonOk);
        Button cancelButton = findViewById(R.id.buttonCancel);

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        this.callback = newCallback;

        this.dialogMode = newMode;

        if (this.dialogMode == Mode.UPDATE) {
            passwordConfirm.setVisibility(View.VISIBLE);
            passwordInput.addTextChangedListener(this);
            passwordConfirm.addTextChangedListener(this);
            passwordConfirm.setOnEditorActionListener(this);
        } else if (this.dialogMode == Mode.ENTER) {
            passwordConfirm.setVisibility(View.GONE);
            passwordInput.setOnEditorActionListener(this);
        }
    }

    // TextWatcher
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (TextUtils.equals(passwordInput.getEditableText(), passwordConfirm.getEditableText()))
            okButton.setEnabled(true);
        else
            okButton.setEnabled(false);
    }

    public void afterTextChanged(Editable s) {}
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorActionHelper.isActionDoneOrKeyboardEnter(actionId, event)) {
            if (okButton.isEnabled()) okButton.performClick();
            return true;
        } else if (EditorActionHelper.isActionUpKeyboardEnter(event)) {
            // Ignore action up after keyboard enter. Otherwise the cancel button would be selected
            // after pressing enter with an invalid password.
            return true;
        }

        return false;
    }

    // View.OnClickListener
    public void onClick(View view)  {
        if (view.getId() == R.id.buttonOk) {
            if (callback != null)
                callback.onPasswordEntered(passwordInput.getText().toString());
        }

        dismiss();
    }
}
