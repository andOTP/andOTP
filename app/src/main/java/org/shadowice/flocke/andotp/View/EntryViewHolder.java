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

package org.shadowice.flocke.andotp.View;

import android.content.Context;
import android.graphics.ColorFilter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.EntryThumbnail;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.Tools;
import org.shadowice.flocke.andotp.View.ItemTouchHelper.ItemTouchHelperViewHolder;

import java.util.List;

public class EntryViewHolder extends RecyclerView.ViewHolder
        implements ItemTouchHelperViewHolder {

    private Context context;
    private Callback callback;
    private boolean tapToReveal;

    private CardView card;
    private LinearLayout valueLayout;
    private LinearLayout coverLayout;
    private LinearLayout customPeriodLayout;
    private ImageView visibleImg;
    private ImageView thumbnailImg;
    private TextView value;
    private TextView label;
    private TextView tags;
    private TextView customPeriod;

    public EntryViewHolder(Context context, final View v, boolean tapToReveal) {
        super(v);

        this.context = context;
        this.tapToReveal = tapToReveal;

        card = v.findViewById(R.id.card_view);
        value = v.findViewById(R.id.valueText);
        valueLayout = v.findViewById(R.id.valueLayout);
        visibleImg = v.findViewById(R.id.valueImg);
        thumbnailImg = v.findViewById(R.id.thumbnailImg);
        coverLayout = v.findViewById(R.id.coverLayout);
        label = v.findViewById(R.id.textViewLabel);
        tags = v.findViewById(R.id.textViewTags);
        customPeriodLayout = v.findViewById(R.id.customPeriodLayout);
        customPeriod = v.findViewById(R.id.customPeriod);

        ImageButton menuButton = v.findViewById(R.id.menuButton);
        ImageButton copyButton = v.findViewById(R.id.copyButton);
        ImageView invisibleImg = v.findViewById(R.id.coverImg);

        // Style the buttons in the current theme colors
        ColorFilter colorFilter = Tools.getThemeColorFilter(context, android.R.attr.textColorSecondary);

        menuButton.getDrawable().setColorFilter(colorFilter);
        copyButton.getDrawable().setColorFilter(colorFilter);
        visibleImg.getDrawable().setColorFilter(colorFilter);
        invisibleImg.getDrawable().setColorFilter(colorFilter);

        // Setup onClickListeners
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onMenuButtonClicked(view, getAdapterPosition());
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onCopyButtonClicked(value.getTag().toString(), getAdapterPosition());
            }
        });

        setTapToReveal(tapToReveal);
    }

    public void updateValues(String label, String token, List<String> tags, EntryThumbnail.EntryThumbnails thumbnail, boolean isVisible) {
        Settings settings = new Settings(context);
        final String tokenFormatted = Tools.formatToken(token, settings.getTokenSplitGroupSize());

        this.label.setText(label);
        value.setText(tokenFormatted);
        // save the unformatted token to the tag of this TextView for copy/paste
        value.setTag(token);

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < tags.size(); i++) {
            stringBuilder.append(tags.get(i));
            if(i < tags.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        this.tags.setText(stringBuilder.toString());

        if (! tags.isEmpty()) {
            this.tags.setVisibility(View.VISIBLE);
        } else {
            this.tags.setVisibility(View.GONE);
        }

        thumbnailImg.setVisibility(settings.getThumbnailVisible() ? View.VISIBLE : View.GONE);

        int thumbnailSize = settings.getThumbnailSize();
        thumbnailImg.setImageBitmap(EntryThumbnail.getThumbnailGraphic(context, label, thumbnailSize, thumbnail));

        if (this.tapToReveal) {
            if (isVisible) {
                valueLayout.setVisibility(View.VISIBLE);
                coverLayout.setVisibility(View.GONE);
                visibleImg.setVisibility(View.GONE);
            } else {
                valueLayout.setVisibility(View.GONE);
                coverLayout.setVisibility(View.VISIBLE);
                visibleImg.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showCustomPeriod(int period) {
        customPeriodLayout.setVisibility(View.VISIBLE);
        customPeriod.setText(String.format(context.getString(R.string.format_custom_period), period));
    }

    public void hideCustomPeriod() {
        customPeriodLayout.setVisibility(View.GONE);
    }

    public void setLabelSize(int size) {
        label.setTextSize(size);
        tags.setTextSize(0.75f * size);
    }

    public void setThumbnailSize(int size) {
        thumbnailImg.getLayoutParams().height = size;
        thumbnailImg.getLayoutParams().width = size;
        thumbnailImg.requestLayout();
    }

    public void setLabelScroll(boolean active) {
        if (active) {
            label.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            label.setHorizontallyScrolling(true);
            label.setSelected(true);
        } else {
            label.setEllipsize(TextUtils.TruncateAt.END);
            label.setHorizontallyScrolling(false);
            label.setSelected(false);
        }
    }

    private void setTapToReveal(boolean enabled) {
        if (enabled) {
            valueLayout.setVisibility(View.GONE);
            coverLayout.setVisibility(View.VISIBLE);
            visibleImg.setVisibility(View.VISIBLE);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onTap(getAdapterPosition());
                }
            });
        } else {
            valueLayout.setVisibility(View.VISIBLE);
            coverLayout.setVisibility(View.GONE);
            visibleImg.setVisibility(View.GONE);

            card.setOnClickListener(null);
        }
    }

    @Override
    public void onItemSelected() {
        if (callback != null)
            callback.onMoveEventStart();
    }

    @Override
    public void onItemClear() {
        if (callback != null)
            callback.onMoveEventStop();
    }

    public void setCallback(Callback cb) {
        this.callback = cb;
    }

    public interface Callback {
        void onMoveEventStart();
        void onMoveEventStop();

        void onMenuButtonClicked(View parentView, int position);
        void onCopyButtonClicked(String text, int position);
        void onTap(int position);
    }
}
