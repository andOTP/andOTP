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

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.View.ItemTouchHelper.ItemTouchHelperAdapter;
import org.shadowice.flocke.andotp.R;

import static org.shadowice.flocke.andotp.Utilities.Settings.SortMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class EntriesCardAdapter extends RecyclerView.Adapter<EntryViewHolder>
    implements ItemTouchHelperAdapter, Filterable {
    private Context context;
    private SharedPreferences sharedPrefs;
    private EntryFilter filter;
    private ArrayList<Entry> entries;
    private ArrayList<Entry> displayedEntries;
    private Callback callback;

    private SortMode sortMode = SortMode.UNSORTED;

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

    private int getRealIndex(int displayPosition) {
        return entries.indexOf(displayedEntries.get(displayPosition));
    }

    public void entriesChanged() {
        displayedEntries = sortEntries(entries);
        notifyDataSetChanged();
    }

    public void saveEntries() {
        DatabaseHelper.saveDatabase(context, entries);
    }

    public void loadEntries() {
        entries = DatabaseHelper.loadDatabase(context);
        entriesChanged();
    }

    @Override
    public void onBindViewHolder(EntryViewHolder entryViewHolder, int i) {
        Entry entry = displayedEntries.get(i);

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

        EntryViewHolder viewHolder = new EntryViewHolder(context, itemView);
        viewHolder.setCallback(new EntryViewHolder.Callback() {
            @Override
            public void onMoveEventStart() {
                if (callback != null)
                    callback.onMoveEventStart();
            }

            @Override
            public void onMoveEventStop() {
                if (callback != null)
                    callback.onMoveEventStop();
            }

            @Override
            public void onMenuButtonClicked(View parentView, int position) {
                showPopupMenu(parentView, position);
            }

            @Override
            public void onCopyButtonClicked(String text) {
                copyToClipboard(text);
            }
        });

        return viewHolder;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (sortMode == SortMode.UNSORTED && displayedEntries.equals(entries)) {
            Collections.swap(entries, fromPosition, toPosition);

            displayedEntries = new ArrayList<>(entries);
            notifyItemMoved(fromPosition, toPosition);

            DatabaseHelper.saveDatabase(context, entries);
        }

        return true;
    }

    public void editEntryLabel(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final EditText input = new EditText(context);
        input.setText(displayedEntries.get(pos).getLabel());
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
                        int realIndex = getRealIndex(pos);
                        String newLabel = input.getEditableText().toString();

                        displayedEntries.get(pos).setLabel(newLabel);
                        if (sortMode == SortMode.LABEL) {
                            displayedEntries = sortEntries(displayedEntries);
                            notifyDataSetChanged();
                        } else {
                            notifyItemChanged(pos);
                        }

                        entries.get(realIndex).setLabel(newLabel);
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

    public void removeItem(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.dialog_title_remove)
                .setMessage(R.string.dialog_msg_confirm_delete)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int realIndex = getRealIndex(pos);

                        displayedEntries.remove(pos);
                        notifyItemRemoved(pos);

                        entries.remove(realIndex);
                        DatabaseHelper.saveDatabase(context, entries);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
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

    public void setSortMode(SortMode mode) {
        this.sortMode = mode;
        entriesChanged();
    }

    public SortMode getSortMode() {
        return this.sortMode;
    }

    private ArrayList<Entry> sortEntries(ArrayList<Entry> unsorted) {
        ArrayList<Entry> sorted = new ArrayList<>(unsorted);

        if (sortMode == SortMode.LABEL) {
            Collections.sort(sorted, new LabelComparator());
        }

        return sorted;
    }

    public void setCallback(Callback cb) {
        this.callback = cb;
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

            ArrayList<Entry> filtered = new ArrayList<>();
            if (constraint != null && constraint.length() != 0){
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).getLabel().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filtered.add(entries.get(i));
                    }
                }
            } else {
                filtered = entries;
            }

            filterResults.count = filtered.size();
            filterResults.values = filtered;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            displayedEntries = sortEntries((ArrayList<Entry>) results.values);
            notifyDataSetChanged();
        }
    }

    public class LabelComparator implements Comparator<Entry> {
        @Override
        public int compare(Entry o1, Entry o2) {
            return o1.getLabel().compareTo(o2.getLabel());
        }
    }

    public interface Callback {
        void onMoveEventStart();
        void onMoveEventStop();
    }
}
