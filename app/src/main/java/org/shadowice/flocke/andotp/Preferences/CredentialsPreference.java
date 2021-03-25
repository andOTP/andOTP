/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
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

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Tasks.ChangeCredentialsTask;
import org.shadowice.flocke.andotp.Utilities.ConfirmedPasswordTransformationHelper;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.EditorActionHelper;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.UIHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.crypto.SecretKey;

import static android.content.Context.KEYGUARD_SERVICE;
import static org.shadowice.flocke.andotp.Utilities.Constants.AuthMethod;
import static org.shadowice.flocke.andotp.Utilities.Constants.EncryptionType;

public class CredentialsPreference extends DialogPreference
    implements AdapterView.OnItemClickListener, View.OnClickListener, TextWatcher, TextView.OnEditorActionListener {
    public static final AuthMethod DEFAULT_VALUE = AuthMethod.NONE;

    private final List<String> entries;
    private static final List<AuthMethod> entryValues = Arrays.asList(
            AuthMethod.NONE,
            AuthMethod.PASSWORD,
            AuthMethod.PIN,
            AuthMethod.DEVICE
    );

    private int minLength = 0;

    private final Context context;
    private final Settings settings;
    private AuthMethod value = AuthMethod.NONE;
    private SecretKey oldEncryptionKey = null;

    private LinearLayout credentialsLayout;
    private ListView credentialsSelection;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordInput;
    private EditText passwordConfirm;
    private TextView tooShortWarning;

    private Button btnCancel;
    private Button btnSave;
    private ProgressBar progressBar;

    private EncryptionHelper.EncryptionChangeCallback encryptionChangeCallback;

    public CredentialsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        this.settings = new Settings(context);
        this.entries = Arrays.asList(context.getResources().getStringArray(R.array.settings_entries_auth));

        setDialogLayoutResource(R.layout.component_authentication);
    }

    public void setOldEncryptionKey(SecretKey oldKey) {
        this.oldEncryptionKey = oldKey;
    }

    public void setEncryptionChangeCallback(EncryptionHelper.EncryptionChangeCallback changeCallback) {
        this.encryptionChangeCallback = changeCallback;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);

        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        value = settings.getAuthMethod();

        credentialsSelection = view.findViewById(R.id.credentialSelection);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_single_choice, entries);
        credentialsSelection.setAdapter(adapter);

        int index = entryValues.indexOf(value);
        credentialsSelection.setSelection(index);
        credentialsSelection.setItemChecked(index,true);
        credentialsSelection.setOnItemClickListener(this);

        credentialsLayout = view.findViewById(R.id.credentialsLayout);

        passwordLayout = view.findViewById(R.id.passwordLayout);
        passwordInput = view.findViewById(R.id.passwordEdit);
        passwordConfirm = view.findViewById(R.id.passwordConfirm);

        if (settings.getBlockAccessibility()) {
            passwordLayout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            passwordConfirm.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && settings.getBlockAutofill()) {
            passwordLayout.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
            passwordConfirm.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        }

        tooShortWarning = view.findViewById(R.id.tooShortWarning);

        passwordInput.addTextChangedListener(this);
        passwordConfirm.addTextChangedListener(this);
        passwordConfirm.setOnEditorActionListener(this);

        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        progressBar = view.findViewById(R.id.saveProgress);

        updateLayout();

        super.onBindDialogView(view);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            String stringValue = getPersistedString(DEFAULT_VALUE.name().toLowerCase(Locale.ENGLISH));
            value = AuthMethod.valueOf(stringValue.toUpperCase(Locale.ENGLISH));
        } else {
            value = DEFAULT_VALUE;
            persistString(value.name().toLowerCase(Locale.ENGLISH));
        }

        setSummary(entries.get(entryValues.indexOf(value)));
    }

    private void saveValues() {
        Constants.EncryptionType encryptionType = settings.getEncryption();

        if (encryptionType == EncryptionType.PASSWORD) {
            if (value == AuthMethod.NONE || value == AuthMethod.DEVICE) {
                UIHelper.showGenericDialog(getContext(), R.string.settings_dialog_title_error, R.string.settings_dialog_msg_auth_invalid_with_encryption);
                return;
            }
        }

        if (value == AuthMethod.DEVICE) {
            KeyguardManager km = (KeyguardManager) getContext().getSystemService(KEYGUARD_SERVICE);

            if (! km.isKeyguardSecure()) {
                Toast.makeText(getContext(), R.string.settings_toast_auth_device_not_secure, Toast.LENGTH_LONG).show();
                return;
            }
        }

        assert passwordInput.getText() != null; // The save button should only be accessible when a password has been entered (and confirmed)
        ChangeCredentialsTask task = new ChangeCredentialsTask(context, encryptionType, oldEncryptionKey, value, passwordInput.getText().toString());
        task.setCallback(this::handleTaskResult);

        setInProgress(true);
        task.execute();
    }

    private void setInProgress(boolean inProgress) {
        credentialsSelection.setEnabled(!inProgress);
        passwordInput.setEnabled(!inProgress);
        passwordConfirm.setEnabled(!inProgress);
        btnCancel.setEnabled(!inProgress);

        btnSave.setVisibility(inProgress ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);

        getDialog().setCancelable(!inProgress);
    }

    private void handleTaskResult(ChangeCredentialsTask.Result result) {
        if (result.success) {
            persistString(value.toString().toLowerCase());
            setSummary(entries.get(entryValues.indexOf(value)));

            if (encryptionChangeCallback != null)
                encryptionChangeCallback.onSuccessfulEncryptionChange(result.encryptionType, result.newEncryptionKey);
        }

        setInProgress(false);
        getDialog().dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.btnCancel):
                getDialog().dismiss();
                break;
            case (R.id.btnSave):
                saveValues();
                break;
            default:
                break;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String password = passwordInput.getEditableText().toString();

        if (password.length() >= minLength) {
            tooShortWarning.setVisibility(View.GONE);
            String confirm = passwordConfirm.getEditableText().toString();

            boolean canSave = !password.isEmpty() && !confirm.isEmpty() && password.equals(confirm);
            btnSave.setEnabled(canSave);
        } else {
            tooShortWarning.setVisibility(View.VISIBLE);
        }
    }

    private void updateLayout() {
        if (value == AuthMethod.NONE || value == AuthMethod.DEVICE) {
            credentialsLayout.setVisibility(View.GONE);
            if (getDialog() != null) {
                UIHelper.hideKeyboard(getContext(), passwordInput);
            }
            btnSave.setEnabled(true);
        } else if (value == AuthMethod.PASSWORD || value == AuthMethod.PIN) {
            prepareAuthMethodInputFields();
        }
    }

    private void prepareAuthMethodInputFields() {
        if (value != AuthMethod.PIN && value != AuthMethod.PASSWORD) {
            return;
        }
        boolean isPassword = value == AuthMethod.PASSWORD;

        credentialsLayout.setVisibility(View.VISIBLE);
        int layoutHintRes = isPassword ? R.string.settings_hint_password : R.string.settings_hint_pin;
        passwordLayout.setHint(getContext().getString(layoutHintRes));
        int confirmHintRes = isPassword ? R.string.settings_hint_password_confirm : R.string.settings_hint_pin_confirm;
        passwordConfirm.setHint(confirmHintRes);

        int inputType = isPassword ?
                (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD) :
                (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        passwordInput.setInputType(inputType);
        passwordConfirm.setInputType(inputType);
        ConfirmedPasswordTransformationHelper.setup(passwordLayout, passwordInput, passwordConfirm);

        minLength = isPassword ? Constants.AUTH_MIN_PASSWORD_LENGTH : Constants.AUTH_MIN_PIN_LENGTH;
        int shortWarningRes = isPassword ? R.string.settings_label_short_password : R.string.settings_label_short_pin;
        tooShortWarning.setText(getContext().getString(shortWarningRes, minLength));

        passwordInput.requestFocus();
        UIHelper.showKeyboard(getContext(), passwordInput);
        btnSave.setEnabled(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        value = entryValues.get(position);
        updateLayout();
        clearInputFields();
    }

    private void clearInputFields() {
        passwordInput.setText(null);
        passwordConfirm.setText(null);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorActionHelper.isActionDoneOrKeyboardEnter(actionId, event)) {
            if (btnSave.isEnabled()) btnSave.performClick();
            return true;
        } else {
            // Ignore action up after keyboard enter. Otherwise the cancel button would be selected
            // after pressing enter with an invalid password.
            return EditorActionHelper.isActionUpKeyboardEnter(event);
        }
    }

    // Needed stub functions
    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}


}
