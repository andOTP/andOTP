package org.shadowice.flocke.andotp.View;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.ColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.EntryThumbnail;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.Tools;

public class EntryViewDialog {
    private Context context;

    private LinearLayout customPeriodLayout;
    private FrameLayout thumbnailFrame;
    private ImageView thumbnailImg;
    private TextView value;
    private TextView label;
    private TextView tags;
    private TextView customPeriod;
    private ImageButton menuButton;
    private ImageButton copyButton;

    private AlertDialog dialog;

    public EntryViewDialog (Context context, ViewGroup viewGroup) {
        this.context = context;

        View v = LayoutInflater.from(context).inflate(R.layout.dialog_entry_view, viewGroup, false);

        customPeriodLayout = v.findViewById(R.id.customPeriodLayout);
        thumbnailFrame = v.findViewById(R.id.thumbnailFrame);
        thumbnailImg = v.findViewById(R.id.thumbnailImg);
        value = v.findViewById(R.id.valueText);
        label = v.findViewById(R.id.textViewLabel);
        tags = v.findViewById(R.id.textViewTags);
        customPeriod = v.findViewById(R.id.customPeriod);
        menuButton = v.findViewById(R.id.menuButton);
        copyButton = v.findViewById(R.id.copyButton);

        ColorFilter colorFilter = Tools.getThemeColorFilter(context, android.R.attr.textColorSecondary);

        if(menuButton != null) menuButton.getDrawable().setColorFilter(colorFilter);
        if(copyButton != null) copyButton.getDrawable().setColorFilter(colorFilter);

        dialog = new AlertDialog.Builder(context)
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                }).create();
    }
    
    public void show(final Entry entry, final int adapterPosition, final EntryViewHolder.Callback callback) {
        String token = entry.getCurrentOTP();
        Settings settings = new Settings(context);
        final String tokenFormatted = Tools.formatToken(token, settings.getTokenSplitGroupSize());

        if(label != null) label.setText(entry.getLabel());
        if(value != null) {
            value.setText(tokenFormatted);
            // save the unformatted token to the tag of this TextView for copy/paste
            value.setTag(token);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < entry.getTags().size(); i++) {
            stringBuilder.append(entry.getTags().get(i));
            if(i < entry.getTags().size() - 1) {
                stringBuilder.append(", ");
            }
        }
        if(tags != null) {
            tags.setText(stringBuilder.toString());

            if (!entry.getTags().isEmpty()) {
                tags.setVisibility(View.VISIBLE);
            } else {
                tags.setVisibility(View.GONE);
            }
        }

        if(thumbnailFrame != null) {
            thumbnailFrame.setVisibility(settings.getThumbnailVisible() ? View.VISIBLE : View.GONE);
        }

        if(thumbnailImg != null) {
            thumbnailImg.setVisibility(settings.getThumbnailVisible() ? View.VISIBLE : View.GONE);

            int thumbnailSize = settings.getThumbnailSize();
            if (settings.getThumbnailVisible()) {
                thumbnailImg.setImageBitmap(EntryThumbnail.getThumbnailGraphic(context, entry.getLabel(), thumbnailSize, entry.getThumbnail()));
            }
        }

        if(entry.hasNonDefaultPeriod()) {
            if(customPeriodLayout != null) customPeriodLayout.setVisibility(View.VISIBLE);
            if(customPeriod != null) customPeriod.setText(String.format(context.getString(R.string.format_custom_period), entry.getPeriod()));
        } else {
            if(customPeriodLayout != null) customPeriodLayout.setVisibility(View.GONE);
        }


        if(menuButton != null) menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onMenuButtonClicked(view, adapterPosition);
            }
        });

        if(copyButton != null) copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onCopyButtonClicked(entry.getCurrentOTP(), adapterPosition);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
