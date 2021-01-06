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

package org.shadowice.flocke.andotp.View;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.ColorFilter;

import androidx.annotation.NonNull;

import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.EntryThumbnail;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.Tools;
import org.shadowice.flocke.andotp.View.ItemTouchHelper.ItemTouchHelperViewHolder;

import java.util.List;
import java.util.Locale;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static org.shadowice.flocke.andotp.Activities.MainActivity.animatorDuration;

public class EntryViewHolder extends RecyclerView.ViewHolder
        implements ItemTouchHelperViewHolder {
    private final Context context;
    private Callback callback;
    private boolean tapToReveal;

    private final CardView card;
    private final LinearLayout valueLayout;
    private final LinearLayout coverLayout;
    private final LinearLayout counterLayout;
    private final FrameLayout thumbnailFrame;
    private final ImageView visibleImg;
    private final ImageView thumbnailImg;
    private final ImageButton menuButton;
    private final ImageButton copyButton;
    private final TextView value;
    private final TextView issuer;
    private final TextView label;
    private final TextView separator;
    private final TextView counter;
    private final TextView tags;
    private final MaterialProgressBar progressBar;

    public EntryViewHolder(Context context, final View v, boolean tapToReveal) {
        super(v);

        this.context = context;

        card = v.findViewById(R.id.card_view);
        value = v.findViewById(R.id.valueText);
        valueLayout = v.findViewById(R.id.valueLayout);
        visibleImg = v.findViewById(R.id.valueImg);
        thumbnailFrame = v.findViewById(R.id.thumbnailFrame);
        thumbnailImg = v.findViewById(R.id.thumbnailImg);
        coverLayout = v.findViewById(R.id.coverLayout);
        issuer = v.findViewById(R.id.textViewIssuer);
        label = v.findViewById(R.id.textViewLabel);
        separator = v.findViewById(R.id.textViewSeparator);
        tags = v.findViewById(R.id.textViewTags);
        counterLayout = v.findViewById(R.id.counterLayout);
        counter = v.findViewById(R.id.counter);
        progressBar = v.findViewById(R.id.cardProgressBar);

        menuButton = v.findViewById(R.id.menuButton);
        copyButton = v.findViewById(R.id.copyButton);
        ImageView invisibleImg = v.findViewById(R.id.coverImg);

        // Style the buttons in the current theme colors
        ColorFilter colorFilter = Tools.getThemeColorFilter(context, android.R.attr.textColorSecondary);

        menuButton.getDrawable().setColorFilter(colorFilter);
        copyButton.getDrawable().setColorFilter(colorFilter);
        visibleImg.getDrawable().setColorFilter(colorFilter);
        invisibleImg.getDrawable().setColorFilter(colorFilter);

        setupOnClickListeners(menuButton, copyButton);

        setTapToReveal(tapToReveal);
    }

    private void setupOnClickListeners(ImageButton menuButton, ImageButton copyButton) {
        menuButton.setOnClickListener(view ->
            adapterPositionSafeCallback((callback, adapterPosition) ->
                callback.onMenuButtonClicked(view, adapterPosition)
            )
        );

        copyButton.setOnClickListener(view ->
            adapterPositionSafeCallback((callback, adapterPosition) ->
                callback.onCopyButtonClicked(value.getTag().toString(), adapterPosition)
            )
        );

        counterLayout.setOnClickListener(view ->
            adapterPositionSafeCallback(Callback::onCounterClicked)
        );

        counterLayout.setOnLongClickListener(view -> {
            adapterPositionSafeCallback(Callback::onCounterLongPressed);
            return false;
        });

        card.setOnClickListener(new SimpleDoubleClickListener() {
            @Override
            public void onSingleClick(View v) {
                adapterPositionSafeCallback((callback, adapterPosition) ->
                    callback.onCardSingleClicked(adapterPosition, value.getTag().toString())
                );
            }

            @Override
            public void onDoubleClick(View v) {
                adapterPositionSafeCallback((callback, adapterPosition) ->
                    callback.onCardDoubleClicked(adapterPosition, value.getTag().toString())
                );
            }
        });
    }

    @FunctionalInterface
    private interface AdapterPositionSafeCallbackConsumer {
        /** The specified {@link Callback} is guaranteed to be non-null, and adapterPosition is
         * guaranteed to be a valid position. */
        void accept(@NonNull Callback callback, int adapterPosition);
    }

    private void adapterPositionSafeCallback(AdapterPositionSafeCallbackConsumer safeCallback) {
        int clickedPosition = getAdapterPosition();
        if (callback != null && clickedPosition != RecyclerView.NO_POSITION) {
            safeCallback.accept(callback, clickedPosition);
        }
    }

    public void updateValues(Entry entry) {
        Settings settings = new Settings(context);

        if (entry.getType() == Entry.OTPType.HOTP) {
            counterLayout.setVisibility(View.VISIBLE);
            counter.setText(String.format(Locale.ENGLISH, "%d", entry.getCounter()));
        } else {
            counterLayout.setVisibility(View.GONE);
        }

        final String tokenFormatted = Tools.formatToken(entry.getCurrentOTP(), settings.getTokenSplitGroupSize());

        String contentHint = "";

        String issuerText = entry.getIssuer();
        if (!TextUtils.isEmpty(issuerText)) {
            issuer.setText(issuerText);
            issuer.setVisibility(View.VISIBLE);

            contentHint = issuerText;
        } else {
            issuer.setVisibility(View.GONE);
        }

        String labelText = entry.getLabel();
        if (!TextUtils.isEmpty(labelText)) {
            label.setText(labelText);
            label.setVisibility(View.VISIBLE);

            contentHint = labelText;
        } else {
            label.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(issuerText) && !TextUtils.isEmpty(labelText)) {
            separator.setVisibility(View.VISIBLE);

            contentHint = issuerText + " - " + labelText;
        } else {
            separator.setVisibility(View.GONE);
        }

        copyButton.setContentDescription(context.getString(R.string.button_card_copy_format, contentHint));
        menuButton.setContentDescription(context.getString(R.string.button_card_options_format, contentHint));

        value.setText(tokenFormatted);
        // save the unformatted token to the tag of this TextView for copy/paste
        value.setTag(entry.getCurrentOTP());

        List<String> entryTags = entry.getTags();

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < entryTags.size(); i++) {
            stringBuilder.append(entryTags.get(i));
            if(i < entryTags.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        tags.setText(stringBuilder.toString());

        tags.setVisibility(entryTags.isEmpty() ? View.GONE : View.VISIBLE);
        thumbnailFrame.setVisibility(settings.getThumbnailVisible() ? View.VISIBLE : View.GONE);

        int thumbnailSize = settings.getThumbnailSize();
        if(settings.getThumbnailVisible()) {
            thumbnailImg.setImageBitmap(EntryThumbnail.getThumbnailGraphic(context, entry.getIssuer(), entry.getLabel(), thumbnailSize, entry.getThumbnail()));
        }

        if (entry.isTimeBased() && (entry.hasNonDefaultPeriod() || settings.isShowIndividualTimeoutsEnabled())) {
            if (!this.tapToReveal || entry.isVisible()) {
                progressBar.setVisibility(View.VISIBLE);
                updateProgress(entry);
            } else {
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            progressBar.setVisibility(View.GONE);
        }

        if (this.tapToReveal) {
            if (entry.isVisible()) {
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

    private void updateProgress(Entry entry) {
        int progress =  (int) (entry.getPeriod() - (System.currentTimeMillis() / 1000) % entry.getPeriod()) ;

        progressBar.setMax(entry.getPeriod() * 100);
        progressBar.setProgress(progress*100);

        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", (progress - 1) * 100);
        animation.setDuration(animatorDuration);
        animation.setInterpolator(new LinearInterpolator());
        animation.start();
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
        tapToReveal = enabled;

        if (enabled) {
            valueLayout.setVisibility(View.GONE);
            coverLayout.setVisibility(View.VISIBLE);
            visibleImg.setVisibility(View.VISIBLE);
        } else {
            valueLayout.setVisibility(View.VISIBLE);
            coverLayout.setVisibility(View.GONE);
            visibleImg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemSelected() {
        if (callback != null)
            callback.onMoveEventStart();
        card.setAlpha(0.5f);
    }

    @Override
    public void onItemClear() {
        if (callback != null)
            callback.onMoveEventStop();
        card.setAlpha(1f);
    }

    public void setCallback(Callback cb) {
        this.callback = cb;
    }

    public interface Callback {
        void onMoveEventStart();
        void onMoveEventStop();

        void onMenuButtonClicked(View parentView, int position);
        void onCopyButtonClicked(String text, int position);

        void onCardSingleClicked(int position, String text);
        void onCardDoubleClicked(int position, String text);

        void onCounterClicked(int position);
        void onCounterLongPressed(int position);
    }
    /**
     * Updates the color of OTP to red (if expiring) or default color (if new OTP)
     *
     * @param color will define if the color needs to be changed to red or default
     * */
    public void updateColor(int color) {
        int textColor;
        if(color == Entry.COLOR_RED) {
            textColor = Tools.getThemeColor(context, R.attr.colorExpiring);
        } else {
            textColor = Tools.getThemeColor(context, android.R.attr.textColorSecondary);
        }

        value.setTextColor(textColor);
    }
}
