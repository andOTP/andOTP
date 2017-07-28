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
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
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
    private EntryFilter filter;
    private ArrayList<Entry> entries;
    private ArrayList<Integer> displayedEntries;
    public ViewHolderEventCallback viewHolderEventCallback;

    public EntriesCardAdapter(Context context) {
        this.context = context;

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

        entryViewHolder.OTPValue.setText(entry.getCurrentOTP());
        entryViewHolder.OTPLabel.setText(entry.getLabel());
        entryViewHolder.eventCallback = viewHolderEventCallback;

        if (entry.hasNonDefaultPeriod()) {
            entryViewHolder.customPeriodLayout.setVisibility(View.VISIBLE);
            entryViewHolder.customPeriod.setText(String.format(context.getString(R.string.format_custom_period), entry.getPeriod()));
        } else {
            entryViewHolder.customPeriodLayout.setVisibility(View.GONE);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean(context.getString(R.string.settings_key_tap_to_reveal), false)) {
            entryViewHolder.enableTapToReveal();
        } else {
            entryViewHolder.disableTapToReveal();
        }
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.component_card, viewGroup, false);

        return new EntryViewHolder(itemView);
    }

    @Override
    public void onItemDismiss(final int position) {
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
                    onItemDismiss(pos);
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
    };

    public class EntryViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder {

        private ViewHolderEventCallback eventCallback;

        protected CardView card;
        protected TextView OTPValue;
        protected TextView OTPValueCover;
        protected TextView OTPLabel;
        protected LinearLayout customPeriodLayout;
        protected TextView customPeriod;
        protected ImageButton menuButton;
        protected ImageButton copyButton;

        public EntryViewHolder(final View v) {
            super(v);

            card = (CardView) v.findViewById(R.id.card_view);
            OTPValue = (TextView) v.findViewById(R.id.textViewOTP);
            OTPValueCover = (TextView) v.findViewById(R.id.textViewOTPCover);
            OTPLabel = (TextView) v.findViewById(R.id.textViewLabel);
            customPeriodLayout = (LinearLayout) v.findViewById(R.id.customPeriodLayout);
            customPeriod = (TextView) v.findViewById(R.id.customPeriod);
            menuButton = (ImageButton) v.findViewById(R.id.menuButton);
            copyButton = (ImageButton) v.findViewById(R.id.copyButton);

            menuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopupMenu(view, getAdapterPosition());
                }
            });

            copyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    copyToClipboard(OTPValue.getText().toString());
                }
            });
        }

        public void enableTapToReveal() {
            OTPValue.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_visibility_visible, 0, 0, 0);
            OTPValue.setVisibility(View.GONE);
            OTPValueCover.setVisibility(View.VISIBLE);

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (OTPValue.getVisibility() == View.GONE && OTPValueCover.getVisibility() == View.VISIBLE) {
                        OTPValue.setVisibility(View.VISIBLE);
                        OTPValueCover.setVisibility(View.GONE);
                    } else {
                        OTPValue.setVisibility(View.GONE);
                        OTPValueCover.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        public void disableTapToReveal() {
            OTPValue.setCompoundDrawables(null, null, null, null);
            OTPValue.setVisibility(View.VISIBLE);
            OTPValueCover.setVisibility(View.GONE);

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
