/*
 * Copyright (C) 2018 Jakob Nixdorf
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

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.KeyguardManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.app.OnNavigationBlockedListener;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.UIHelper;

public class IntroScreenActivity extends IntroActivity {
    private Settings settings;

    private EncryptionFragment encryptionFragment;
    private AuthenticationFragment authenticationFragment;

    private void saveSettings() {
        Constants.EncryptionType encryptionType = encryptionFragment.getEncryptionType();
        Constants.AuthMethod authMethod = authenticationFragment.getAuthMethod();

        settings.setEncryption(encryptionType);
        settings.setAuthMethod(authMethod);

        if (authMethod == Constants.AuthMethod.PASSWORD || authMethod == Constants.AuthMethod.PIN) {
            String password = authenticationFragment.getPassword();
            settings.setAuthCredentials(password);
        }

        settings.setFirstTimeWarningShown(true);
    }

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        settings = new Settings(this);

        encryptionFragment = new EncryptionFragment();
        authenticationFragment = new AuthenticationFragment();

        encryptionFragment.setEncryptionChangedCallback(new EncryptionFragment.EncryptionChangedCallback() {
            @Override
            public void onEncryptionChanged(Constants.EncryptionType newEncryptionType) {
                authenticationFragment.updateEncryptionType(newEncryptionType);
            }
        });

        setButtonBackFunction(BUTTON_BACK_FUNCTION_BACK);

        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide1_title)
                .description(R.string.intro_slide1_desc)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .canGoBackward(false)
                .scrollable(false)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .fragment(encryptionFragment)
                .build()
        );

        // Tell the fragment where it is located
        authenticationFragment.setSlidePos(getSlides().size());

        addSlide(new FragmentSlide.Builder()
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .fragment(authenticationFragment)
                .build()
        );

        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide4_title)
                .description(R.string.intro_slide4_desc)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .build()
        );

        addOnNavigationBlockedListener(new OnNavigationBlockedListener() {
            @Override
            public void onNavigationBlocked(int position, int direction) {
                if (position == 2)
                    authenticationFragment.flashWarning();
            }
        });

        addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 3)
                    saveSettings();
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        // We don't want users to quit the intro screen and end up in an uninitialized state
    }

    public static class EncryptionFragment extends SlideFragment {
        private EncryptionChangedCallback encryptionChangedCallback = null;

        private Spinner selection;
        private TextView desc;

        private SparseArray<Constants.EncryptionType> selectionMapping;

        public EncryptionFragment() {
        }

        public void setEncryptionChangedCallback(EncryptionChangedCallback cb) {
            encryptionChangedCallback = cb;
        }

        private void generateSelectionMapping() {
            String[] encValues = getResources().getStringArray(R.array.settings_values_encryption);

            selectionMapping = new SparseArray<>();
            for (int i = 0; i < encValues.length; i++)
                selectionMapping.put(i, Constants.EncryptionType.valueOf(encValues[i].toUpperCase()));
        }

        public Constants.EncryptionType getEncryptionType() {
            return selectionMapping.get(selection.getSelectedItemPosition());
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.component_intro_encryption, container, false);

            selection = root.findViewById(R.id.introEncryptionSelection);
            desc = root.findViewById(R.id.introEncryptionDesc);

            generateSelectionMapping();

            selection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Constants.EncryptionType encryptionType = selectionMapping.get(i);

                    if (encryptionType == Constants.EncryptionType.PASSWORD)
                        desc.setText(R.string.intro_slide2_desc_password);
                    else if (encryptionType == Constants.EncryptionType.KEYSTORE)
                        desc.setText(R.string.intro_slide2_desc_keystore);

                    if (encryptionChangedCallback != null)
                        encryptionChangedCallback.onEncryptionChanged(encryptionType);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            return root;
        }

        public interface EncryptionChangedCallback {
            void onEncryptionChanged(Constants.EncryptionType newEncryptionType);
        }
    }

    public static class AuthenticationFragment extends SlideFragment {
        private Constants.EncryptionType encryptionType = Constants.EncryptionType.KEYSTORE;

        private int slidePos = -1;

        private int minLength = Constants.AUTH_MIN_PASSWORD_LENGTH;
        private String lengthWarning = "";
        private String noPasswordWarning = "";
        private String confirmPasswordWarning = "";

        private TextView desc = null;
        private Spinner selection = null;
        private TextView authWarnings = null;
        private LinearLayout credentialsLayout = null;
        private TextInputLayout passwordLayout = null;
        private TextInputEditText passwordInput = null;
        private EditText passwordConfirm = null;

        private SparseArray<Constants.AuthMethod> selectionMapping;

        public AuthenticationFragment() {
        }

        public void setSlidePos(int pos) {
            slidePos = pos;
        }

        public void updateEncryptionType(Constants.EncryptionType encryptionType) {
            this.encryptionType = encryptionType;

            if (desc != null) {
                if (encryptionType == Constants.EncryptionType.KEYSTORE) {
                    desc.setText(R.string.intro_slide3_desc_keystore);
                } else if (encryptionType == Constants.EncryptionType.PASSWORD) {
                    desc.setText(R.string.intro_slide3_desc_password);

                    Constants.AuthMethod selectedMethod = selectionMapping.get(selection.getSelectedItemPosition());
                    if (selectedMethod != Constants.AuthMethod.PASSWORD && selectedMethod != Constants.AuthMethod.PIN )
                        selection.setSelection(selectionMapping.indexOfValue(Constants.AuthMethod.PASSWORD));
                }
            }
        }

        private void generateSelectionMapping() {
            Constants.AuthMethod[] authValues = Constants.AuthMethod.values();

            selectionMapping = new SparseArray<>();
            for (int i = 0; i < authValues.length; i++)
                selectionMapping.put(i, authValues[i]);
        }

        private void displayWarning(int resId) {
            displayWarning(getString(resId));
        }

        private void displayWarning(String warning) {
            authWarnings.setVisibility(View.VISIBLE);
            authWarnings.setText(warning);
        }

        private void hideWarning() {
            authWarnings.setVisibility(View.GONE);
        }

        public void flashWarning() {
            if (authWarnings.getVisibility() == View.VISIBLE) {
                ObjectAnimator animator = ObjectAnimator.ofInt(authWarnings, "backgroundColor",
                        Color.TRANSPARENT, getResources().getColor(R.color.colorAccent), Color.TRANSPARENT);
                animator.setDuration(500);
                animator.setRepeatCount(0);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setEvaluator(new ArgbEvaluator());
                animator.start();
            }
        }

        public Constants.AuthMethod getAuthMethod() {
            return selectionMapping.get(selection.getSelectedItemPosition());
        }

        public String getPassword() {
            return passwordInput.getText().toString();
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View root = inflater.inflate(R.layout.component_intro_authentication, container, false);

            desc = root.findViewById(R.id.introAuthDesc);
            selection = root.findViewById(R.id.introAuthSelection);
            authWarnings = root.findViewById(R.id.introAuthWarnings);
            credentialsLayout = root.findViewById(R.id.introCredentialsLayout);
            passwordLayout = root.findViewById(R.id.introPasswordLayout);
            passwordInput = root.findViewById(R.id.introPasswordEdit);
            passwordConfirm = root.findViewById(R.id.introPasswordConfirm);

            generateSelectionMapping();

            final String[] authEntries = getResources().getStringArray(R.array.settings_entries_auth);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getIntroActivity(), android.R.layout.simple_spinner_item, authEntries) {
                @Override
                public boolean isEnabled(int position){
                        return encryptionType != Constants.EncryptionType.PASSWORD ||
                                position == selectionMapping.indexOfValue(Constants.AuthMethod.PASSWORD) ||
                                position == selectionMapping.indexOfValue(Constants.AuthMethod.PIN);
                }

                @Override
                public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;

                    tv.setEnabled(encryptionType != Constants.EncryptionType.PASSWORD ||
                            position == selectionMapping.indexOfValue(Constants.AuthMethod.PASSWORD) ||
                            position == selectionMapping.indexOfValue(Constants.AuthMethod.PIN));

                    return view;
                }
            };

            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            selection.setAdapter(spinnerArrayAdapter);

            selection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Constants.AuthMethod authMethod = selectionMapping.get(i);

                    if (authMethod == Constants.AuthMethod.PASSWORD) {
                        credentialsLayout.setVisibility(View.VISIBLE);

                        passwordLayout.setHint(getString(R.string.settings_hint_password));
                        passwordConfirm.setHint(R.string.settings_hint_password_confirm);

                        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        passwordInput.setTransformationMethod(new PasswordTransformationMethod());
                        passwordConfirm.setTransformationMethod(new PasswordTransformationMethod());

                        minLength = Constants.AUTH_MIN_PASSWORD_LENGTH;
                        lengthWarning = getString(R.string.settings_label_short_password, minLength);
                        noPasswordWarning = getString(R.string.intro_slide3_warn_no_password);
                        confirmPasswordWarning = getString(R.string.intro_slide3_warn_confirm_password);

                        if (getIntroActivity().getCurrentSlidePosition() == slidePos) {
                            passwordInput.requestFocus();
                            UIHelper.showKeyboard(getContext(), passwordInput);
                        }
                    } else if (authMethod == Constants.AuthMethod.PIN) {
                        credentialsLayout.setVisibility(View.VISIBLE);

                        passwordLayout.setHint(getString(R.string.settings_hint_pin));
                        passwordConfirm.setHint(R.string.settings_hint_pin_confirm);

                        passwordInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                        passwordConfirm.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

                        passwordInput.setTransformationMethod(new PasswordTransformationMethod());
                        passwordConfirm.setTransformationMethod(new PasswordTransformationMethod());

                        minLength = Constants.AUTH_MIN_PIN_LENGTH;
                        lengthWarning = getString(R.string.settings_label_short_pin, minLength);
                        noPasswordWarning = getString(R.string.intro_slide3_warn_no_pin);
                        confirmPasswordWarning = getString(R.string.intro_slide3_warn_confirm_pin);

                        if (getIntroActivity().getCurrentSlidePosition() == slidePos) {
                            passwordInput.requestFocus();
                            UIHelper.showKeyboard(getContext(), passwordInput);
                        }
                    } else {
                        credentialsLayout.setVisibility(View.INVISIBLE);

                        UIHelper.hideKeyboard(getIntroActivity(), root);
                    }

                    updateNavigation();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    updateNavigation();
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            };

            passwordInput.addTextChangedListener(textWatcher);
            passwordConfirm.addTextChangedListener(textWatcher);

            return root;
        }

        @Override
        public boolean canGoForward() {
            Constants.AuthMethod authMethod = selectionMapping.get(selection.getSelectedItemPosition());

            if (authMethod == Constants.AuthMethod.PIN || authMethod == Constants.AuthMethod.PASSWORD) {
                String password = passwordInput.getText().toString();
                String confirm = passwordConfirm.getText().toString();

                if (! password.isEmpty()) {
                    if (password.length() < minLength) {
                        displayWarning(lengthWarning);
                        return false;
                    } else {
                        if (! confirm.isEmpty() && confirm.equals(password)) {
                            hideWarning();
                            return true;
                        } else {
                            displayWarning(confirmPasswordWarning);
                            return false;
                        }
                    }
                } else {
                    displayWarning(noPasswordWarning);
                    return false;
                }
            } else if (authMethod == Constants.AuthMethod.DEVICE) {
                KeyguardManager km = (KeyguardManager) getContext().getSystemService(KEYGUARD_SERVICE);

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                    displayWarning(R.string.settings_toast_auth_device_pre_lollipop);
                    return false;
                } else if (! km.isKeyguardSecure()) {
                    displayWarning(R.string.settings_toast_auth_device_not_secure);
                    return false;
                }

                hideWarning();

                return true;
            } else {
                hideWarning();

                return true;
            }
        }
    }
}
