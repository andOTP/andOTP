package org.shadowice.flocke.andotp.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.shadowice.flocke.andotp.R;

public class AboutFragment extends BaseAboutFragment {
    private static final String ABOUT_AUTHOR1_EXTRA_LINK = "https://flocke.shadowice.org/donate.html";
    private static final String ABOUT_AUTHOR2_EXTRA_LINK = "https://richyhbm.co.uk/donate";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        LinearLayout author1Content = v.findViewById(R.id.aboutLayoutAuthor1Content);
        LinearLayout author2Content = v.findViewById(R.id.aboutLayoutAuthor2Content);

        View extra1 = inflater.inflate(R.layout.part_author_extra, null);
        View extra2 = inflater.inflate(R.layout.part_author_extra, null);

        extra1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openURI(ABOUT_AUTHOR1_EXTRA_LINK);
                } catch (Exception ignored) {
                    copyToClipboard(ABOUT_AUTHOR1_EXTRA_LINK);
                }
            }
        });

        extra2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    openURI(ABOUT_AUTHOR2_EXTRA_LINK);
                } catch (Exception ignored) {
                    copyToClipboard(ABOUT_AUTHOR2_EXTRA_LINK);
                }
            }
        });

        author1Content.addView(extra1);
        author2Content.addView(extra2);

        return v;
    }
}
