package org.shadowice.flocke.andotp.View;

import android.content.Context;
import android.graphics.ColorFilter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.shadowice.flocke.andotp.Utilities.ThemeHelper;
import org.shadowice.flocke.andotp.View.ItemTouchHelper.ItemTouchHelperViewHolder;
import org.shadowice.flocke.andotp.R;

public class EntryViewHolder extends RecyclerView.ViewHolder
        implements ItemTouchHelperViewHolder {

    private Context context;

    private Callback callback;

    private CardView card;
    private LinearLayout valueLayout;
    private LinearLayout coverLayout;
    private LinearLayout customPeriodLayout;
    private ImageView visibleImg;
    private TextView value;
    private TextView label;
    private TextView customPeriod;


    public EntryViewHolder(Context context, final View v) {
        super(v);

        this.context = context;

        card = (CardView) v.findViewById(R.id.card_view);
        value = (TextView) v.findViewById(R.id.valueText);
        valueLayout = (LinearLayout) v.findViewById(R.id.valueLayout);
        visibleImg = (ImageView) v.findViewById(R.id.valueImg);
        coverLayout = (LinearLayout) v.findViewById(R.id.coverLayout);
        label = (TextView) v.findViewById(R.id.textViewLabel);
        customPeriodLayout = (LinearLayout) v.findViewById(R.id.customPeriodLayout);
        customPeriod = (TextView) v.findViewById(R.id.customPeriod);

        ImageButton menuButton = (ImageButton) v.findViewById(R.id.menuButton);
        ImageButton copyButton = (ImageButton) v.findViewById(R.id.copyButton);
        ImageView invisibleImg = (ImageView) v.findViewById(R.id.coverImg);

        // Style the buttons in the current theme colors
        ColorFilter colorFilter = ThemeHelper.getThemeColorFilter(context, android.R.attr.textColorSecondary);

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
                    callback.onCopyButtonClicked(value.getText().toString());
            }
        });
    }

    public void updateValues(String label, String token) {
        this.label.setText(label);
        value.setText(token);
    }

    public void showCustomPeriod(int period) {
        customPeriodLayout.setVisibility(View.VISIBLE);
        customPeriod.setText(String.format(context.getString(R.string.format_custom_period), period));
    }

    public void hideCustomPeriod() {
        customPeriodLayout.setVisibility(View.GONE);
    }

    public void setLabelSize(int size) {
        label.setTextSize(TypedValue.COMPLEX_UNIT_PT, size);
    }

    public void enableTapToReveal() {
        valueLayout.setVisibility(View.GONE);
        coverLayout.setVisibility(View.VISIBLE);
        visibleImg.setVisibility(View.VISIBLE);

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (valueLayout.getVisibility() == View.GONE && coverLayout.getVisibility() == View.VISIBLE) {
                    valueLayout.setVisibility(View.VISIBLE);
                    coverLayout.setVisibility(View.GONE);
                } else {
                    valueLayout.setVisibility(View.GONE);
                    coverLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void disableTapToReveal() {
        valueLayout.setVisibility(View.VISIBLE);
        coverLayout.setVisibility(View.GONE);
        visibleImg.setVisibility(View.GONE);

        card.setOnClickListener(null);
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
        void onCopyButtonClicked(String text);
    }
}
