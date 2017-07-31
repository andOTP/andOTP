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

package org.shadowice.flocke.andotp;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.shadowice.flocke.andotp.ItemTouchHelper.ItemTouchHelperAdapter;
import org.shadowice.flocke.andotp.ItemTouchHelper.ItemTouchHelperViewHolder;

import java.util.ArrayList;
import java.util.Collections;

public class EntriesCardAdapter extends RecyclerView.Adapter<EntriesCardAdapter.EntryViewHolder>
    implements ItemTouchHelperAdapter, Filterable {

    private Context context;
    private SharedPreferences sharedPrefs;
    private EntryFilter filter;
    private ArrayList<Entry> entries;
    private ArrayList<Integer> displayedEntries;
    public ViewHolderEventCallback viewHolderEventCallback;

    public EntriesCardAdapter(Context context) {
        this.context = context;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        loadEntries();
    }

    @Override
    public int getItemCount() {
        return displayedEntries.size();
    }

    public int getFullItemCount() {
        return entries.size();
    }

    public Entry getItem(int i) {
        return entries.get(i);
    }

    public void addEntry(Entry e) {
        entries.add(e);
        entriesChanged();
    }

    public void entriesChanged() {
        if (displayedEntries != null)
            displayedEntries.clear();
        displayedEntries = defaultIndices();

        notifyDataSetChanged();
    }

    public void saveEntries() {
        DatabaseHelper.saveDatabase(context, entries);
    }

    public void loadEntries() {
        entries = DatabaseHelper.loadDatabase(context);
        entriesChanged();
    }

    public ArrayList<Integer> defaultIndices() {
        ArrayList<Integer> newIdx = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++)
            newIdx.add(i);

        return newIdx;
    }

    public int removeIndex(int pos) {
        int removed = displayedEntries.remove(pos);

        ArrayList<Integer> newIdx = new ArrayList<>();
        for (int i = 0; i < displayedEntries.size(); i++) {
            int idx = displayedEntries.get(i);
            if (idx > removed)
                idx -= 1;
            newIdx.add(idx);
        }
        displayedEntries = newIdx;

        return removed;
    }

    @Override
    public void onBindViewHolder(EntryViewHolder entryViewHolder, int i) {
        Entry entry = entries.get(displayedEntries.get(i));

        entryViewHolder.updateValues(entry.getLabel(), entry.getCurrentOTP());

        if (entry.hasNonDefaultPeriod()) {
            entryViewHolder.showCustomPeriod(entry.getPeriod());
        } else {
            entryViewHolder.hideCustomPeriod();
        }

        if (sharedPrefs.getBoolean(context.getString(R.string.settings_key_tap_to_reveal), false)) {
            entryViewHolder.enableTapToReveal();
        } else {
            entryViewHolder.disableTapToReveal();
        }

        int fontSize = sharedPrefs.getInt(context.getString(R.string.settings_key_label_size), context.getResources().getInteger(R.integer.settings_default_label_size));
        entryViewHolder.setLabelSize(fontSize);
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.component_card, viewGroup, false);

        EntryViewHolder viewHolder = new EntryViewHolder(itemView);
        viewHolder.eventCallback = viewHolderEventCallback;

        return viewHolder;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(entries, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);

        DatabaseHelper.saveDatabase(context, entries);

        return true;
    }

    public void editEntryLabel(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final EditText input = new EditText(context);
        input.setText(entries.get(displayedEntries.get(pos)).getLabel());
        input.setSingleLine();

        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = context.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        params.rightMargin = context.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setTitle(R.string.dialog_title_rename)
                .setView(container)
                .setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        entries.get(displayedEntries.get(pos)).setLabel(input.getEditableText().toString());
                        notifyItemChanged(pos);

                        DatabaseHelper.saveDatabase(context, entries);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create()
                .show();
    }

    public void removeItem(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.dialog_title_remove)
                .setMessage(R.string.dialog_msg_confirm_delete)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        entries.remove(removeIndex(position));
                        notifyItemRemoved(position);

                        DatabaseHelper.saveDatabase(context, entries);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        notifyItemChanged(position);
                    }
                })
                .show();
    }

    private void showPopupMenu(View view, final int pos) {
        View menuItemView = view.findViewById(R.id.menuButton);
        PopupMenu popup = new PopupMenu(view.getContext(), menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.menu_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.menu_popup_editLabel) {
                    editEntryLabel(pos);
                    return true;
                } else if (id == R.id.menu_popup_remove) {
                    removeItem(pos);
                    return true;
                } else {
                    return false;
                }
            }
        });
        popup.show();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.label_clipboard_content), text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_LONG).show();
    }

    public void setMoveEventCallback(ViewHolderEventCallback cb) {
        this.viewHolderEventCallback = cb;
    }

    public EntryFilter getFilter() {
        if (filter == null)
            filter = new EntryFilter();

        return filter;
    }

    public class EntryFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults filterResults = new FilterResults();

            ArrayList<Integer> newIdx = new ArrayList<>();
            if (constraint != null && constraint.length() != 0){
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).getLabel().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        newIdx.add(i);
                    }
                }
            } else {
                newIdx = defaultIndices();
            }

            filterResults.count = newIdx.size();
            filterResults.values = newIdx;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            displayedEntries = (ArrayList<Integer>) results.values;
            notifyDataSetChanged();
        }
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder {

        private ViewHolderEventCallback eventCallback;

        private CardView card;

        private TextView value;
        private LinearLayout valueLayout;
        private ImageView visibleImg;
        
        private LinearLayout coverLayout;

        private TextView label;

        private LinearLayout customPeriodLayout;
        private TextView customPeriod;


        public EntryViewHolder(final View v) {
            super(v);

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
                    showPopupMenu(view, getAdapterPosition());
                }
            });

            copyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    copyToClipboard(value.getText().toString());
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
            if (eventCallback != null)
                eventCallback.onMoveEventStart();
        }

        @Override
        public void onItemClear() {
            if (eventCallback != null)
                eventCallback.onMoveEventStop();
        }
    }

    public interface ViewHolderEventCallback {
        void onMoveEventStart();
        void onMoveEventStop();
    }
}
