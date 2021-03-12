package org.shadowice.flocke.andotp.View.AutoFillable;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.autofill.AutofillValue;

import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@link TextInputEditText} that allows setting a {@link AutoFillTextListener} for getting notified when
 * this field was filled in by autofill.
 *
 * On devices running Android 7.1 (API 25) and below the listener will never be called, because
 * autofill is not supported.
 */
public class AutoFillableTextInputEditText extends TextInputEditText {

    @Nullable private AutoFillTextListener listener = null;

    public AutoFillableTextInputEditText(@NonNull Context context) {
        super(context);
    }

    public AutoFillableTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFillableTextInputEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void autofill(AutofillValue value) {
        super.autofill(value);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || listener == null) {
            return;
        }

        if (value != null && value.isText()) {
            listener.onTextAutoFilled(value.getTextValue());
        }
    }

    public void setAutoFillTextListener(@Nullable AutoFillTextListener listener) {
        this.listener = listener;
    }

    public interface AutoFillTextListener {

        void onTextAutoFilled(@NonNull CharSequence text);
    }
}
