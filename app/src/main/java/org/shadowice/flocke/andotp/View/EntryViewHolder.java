/*
 * Copyright (C) 2017-2018 Jakob Nixdorf
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
import org.shadowice.flocke.andotp.View.ItemTouchHelper.ItemTouchHelperViewHolder;

import java.util.List;
import java.util.Locale;

public class EntryViewHolder extends RecyclerView.ViewHolder
        implements ItemTouchHelperViewHolder {
    private Context context;
    private Callback callback;
    private boolean tapToReveal;

    private CardView card;
    private LinearLayout valueLayout;
    private LinearLayout coverLayout;
    private LinearLayout customPeriodLayout;
    private LinearLayout counterLayout;
    private FrameLayout thumbnailFrame;
    private ImageView visibleImg;
    private ImageView thumbnailImg;
    private TextView value;
    private TextView label;
    private TextView counter;
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
        thumbnailFrame = v.findViewById(R.id.thumbnailFrame);
        thumbnailImg = v.findViewById(R.id.thumbnailImg);
        coverLayout = v.findViewById(R.id.coverLayout);
        label = v.findViewById(R.id.textViewLabel);
        tags = v.findViewById(R.id.textViewTags);
        customPeriodLayout = v.findViewById(R.id.customPeriodLayout);
        customPeriod = v.findViewById(R.id.customPeriod);
        counterLayout = v.findViewById(R.id.counterLayout);
        counter = v.findViewById(R.id.counter);

        ImageButton menuButton = v.findViewById(R.id.menuButton);
        ImageButton copyButton = v.findViewById(R.id.copyButton);
        ImageView invisibleImg = v.findViewById(R.id.coverImg);

        // Style the buttons in the current theme colors
        ColorFilter colorFilter = Tools.getThemeColorFilter(context, android.R.attr.textColorSecondary);

        if(menuButton != null) menuButton.getDrawable().setColorFilter(colorFilter);
        if(copyButton != null) copyButton.getDrawable().setColorFilter(colorFilter);
        if(visibleImg != null) visibleImg.getDrawable().setColorFilter(colorFilter);
        if(invisibleImg != null) invisibleImg.getDrawable().setColorFilter(colorFilter);

        // Setup onClickListeners
        if(menuButton != null) menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onMenuButtonClicked(view, getAdapterPosition());
            }
        });

        if(copyButton != null) copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onCopyButtonClicked(value.getTag().toString(), getAdapterPosition());
            }
        });

        counterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback != null)
                    callback.onCounterTapped(getAdapterPosition());
            }
        });

        counterLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (callback != null)
                    callback.onCounterLongPressed(getAdapterPosition());

                return false;
            }
        });

        setTapToReveal(tapToReveal);

        if(card != null) {
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onTap(getAdapterPosition());
                }
            });
        }
    }

    public void updateValues(Entry entry, boolean showAsPopup) {
        Settings settings = new Settings(context);

        if (entry.getType() == Entry.OTPType.HOTP) {
            if(counterLayout != null) counterLayout.setVisibility(View.VISIBLE);
            if(counter != null) counter.setText(String.format(Locale.ENGLISH, "%d", entry.getCounter()));
        } else {
            if(counterLayout != null) counterLayout.setVisibility(View.GONE);
        }

        final String tokenFormatted = Tools.formatToken(entry.getCurrentOTP(), settings.getTokenSplitGroupSize());

        if(this.label != null) this.label.setText(entry.getLabel());
        if(value != null) {
        	value.setText(tokenFormatted);
        	// save the unformatted token to the tag of this TextView for copy/paste
        	value.setTag(entry.getCurrentOTP());
		}
        List<String> tags = entry.getTags();

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < tags.size(); i++) {
            stringBuilder.append(tags.get(i));
            if(i < tags.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        if(this.tags != null) {
            this.tags.setText(stringBuilder.toString());

            if (!tags.isEmpty()) {
                this.tags.setVisibility(View.VISIBLE);
            } else {
                this.tags.setVisibility(View.GONE);
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

        if (this.tapToReveal) {
            if (entry.isVisible()) {
                if(valueLayout != null) valueLayout.setVisibility(View.VISIBLE);
                if(coverLayout != null) coverLayout.setVisibility(View.GONE);
                if(visibleImg != null) visibleImg.setVisibility(View.GONE);
            } else {
                if(valueLayout != null) valueLayout.setVisibility(View.GONE);
                if(coverLayout != null) coverLayout.setVisibility(View.VISIBLE);
                if(visibleImg != null) visibleImg.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showCustomPeriod(int period) {
        if(customPeriodLayout != null) customPeriodLayout.setVisibility(View.VISIBLE);
        if(customPeriod != null) customPeriod.setText(String.format(Locale.ENGLISH, context.getString(R.string.format_custom_period), period));
    }

    public void hideCustomPeriod() {
        if(customPeriodLayout != null) customPeriodLayout.setVisibility(View.GONE);
    }

    public void setLabelSize(int size) {
        if(label != null) label.setTextSize(size);
        if(tags != null) tags.setTextSize(0.75f * size);
    }

    public void setThumbnailSize(int size) {
        if(label != null) {
            thumbnailImg.getLayoutParams().height = size;
            thumbnailImg.getLayoutParams().width = size;
            thumbnailImg.requestLayout();
        }
    }

    public void setLabelScroll(boolean active) {
        if(label != null) {
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
    }

    private void setTapToReveal(boolean enabled) {
        if (enabled) {
            if(valueLayout != null) valueLayout.setVisibility(View.GONE);
            if(coverLayout != null) coverLayout.setVisibility(View.VISIBLE);
            if(visibleImg != null) visibleImg.setVisibility(View.VISIBLE);
        } else {
            if(valueLayout != null) valueLayout.setVisibility(View.VISIBLE);
            if(coverLayout != null) coverLayout.setVisibility(View.GONE);
            if(visibleImg != null) visibleImg.setVisibility(View.GONE);
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
        void onCounterTapped(int position);
        void onCounterLongPressed(int position);
    }
}
