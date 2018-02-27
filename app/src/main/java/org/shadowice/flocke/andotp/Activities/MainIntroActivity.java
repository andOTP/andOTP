package org.shadowice.flocke.andotp.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.Settings;

public class MainIntroActivity extends IntroActivity {
    private Settings settings;

    private EncryptionFragment encryptionFragment;

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        settings = new Settings(this);

        encryptionFragment = new EncryptionFragment();

        setButtonBackFunction(BUTTON_BACK_FUNCTION_BACK);

        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide1_title)
                .description(R.string.intro_slide1_desc)
                .image(R.mipmap.ic_launcher)
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

        // TODO: Set authentication
        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide1_title)
                .description(R.string.intro_slide1_desc)
                .image(R.mipmap.ic_launcher)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .scrollable(false)
                .build()
        );

        addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    // TODO: Inter-page communication
                    settings.setEncryption(encryptionFragment.getSelectedEncryption());
                    Log.d("INTRO", "Encryption saved");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (getCurrentSlidePosition() != 0)
            super.onBackPressed();
    }

    public static class EncryptionFragment extends SlideFragment {
        private Spinner selection;
        private TextView desc;

        private SparseArray<Constants.EncryptionType> selectionMapping;

        public EncryptionFragment() {
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.component_intro_encryption, container, false);

            selection = root.findViewById(R.id.introEncryptionSelection);
            desc = root.findViewById(R.id.introEncryptionDesc);

            final String[] encValues = getResources().getStringArray(R.array.settings_values_encryption);

            selectionMapping = new SparseArray<>();
            for (int i = 0; i < encValues.length; i++)
                selectionMapping.put(i, Constants.EncryptionType.valueOf(encValues[i].toUpperCase()));

            selection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Constants.EncryptionType encryptionType = selectionMapping.get(i);

                    if (encryptionType == Constants.EncryptionType.PASSWORD)
                        desc.setText(R.string.intro_slide2_desc_password);
                    else if (encryptionType == Constants.EncryptionType.KEYSTORE)
                        desc.setText(R.string.intro_slide2_desc_keystore);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            return root;
        }

        private Constants.EncryptionType getSelectedEncryption() {
            return selectionMapping.get(selection.getSelectedItemPosition());
        }
    }
}
