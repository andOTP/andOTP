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

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.shadowice.flocke.andotp.Activities.MainActivity;
import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.Dialogs.TagsDialog;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.BackupHelper;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.EntryThumbnail;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.Tools;
import org.shadowice.flocke.andotp.View.ItemTouchHelper.ItemTouchHelperAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import javax.crypto.SecretKey;

import static org.shadowice.flocke.andotp.Utilities.Constants.SortMode;

public class EntriesCardAdapter extends RecyclerView.Adapter<EntryViewHolder>
    implements ItemTouchHelperAdapter, Filterable {
    private Context context;
    private Handler taskHandler;
    private EntryFilter filter;
    private ArrayList<Entry> entries;
    private ArrayList<Entry> displayedEntries;
    private Callback callback;
    private List<String> tagsFilter = new ArrayList<>();

    private SecretKey encryptionKey = null;

    private SortMode sortMode = SortMode.UNSORTED;
    private TagsAdapter tagsFilterAdapter;
    private Settings settings;

    public EntriesCardAdapter(Context context, TagsAdapter tagsFilterAdapter) {
        this.context = context;
        this.tagsFilterAdapter = tagsFilterAdapter;
        this.settings = new Settings(context);
        this.taskHandler = new Handler();
        this.entries = new ArrayList<>();
    }

    public void setEncryptionKey(SecretKey key) {
        encryptionKey = key;
    }

    public SecretKey getEncryptionKey() {
        return encryptionKey;
    }

    @Override
    public int getItemCount() {
        return displayedEntries.size();
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public void addEntry(Entry e) {
        if (! entries.contains(e)) {
            entries.add(e);
            entriesChanged();
            saveEntries(settings.getAutoBackupEncryptedPasswordsEnabled());
        } else {
            Toast.makeText(context, R.string.toast_entry_exists, Toast.LENGTH_LONG).show();
        }
    }

    private int getRealIndex(int displayPosition) {
        return entries.indexOf(displayedEntries.get(displayPosition));
    }

    private void entriesChanged() {
        displayedEntries = sortEntries(entries);
        filterByTags(tagsFilter);
        notifyDataSetChanged();
    }

    public void saveEntries(boolean auto_backup) {
        DatabaseHelper.saveDatabase(context, entries, encryptionKey);

        if(auto_backup) {
            Constants.BackupType backupType = BackupHelper.autoBackupType(context);
            if (backupType == Constants.BackupType.ENCRYPTED) {
                Uri backupFilename = Tools.buildUri(settings.getBackupDir(), BackupHelper.backupFilename(context, Constants.BackupType.ENCRYPTED));

                byte[] keyMaterial = encryptionKey.getEncoded();
                SecretKey encryptionKey = EncryptionHelper.generateSymmetricKey(keyMaterial);

                boolean success = BackupHelper.backupToFile(context, backupFilename, settings.getBackupPasswordEnc(), encryptionKey);
                if (success) {
                    Toast.makeText(context, R.string.backup_toast_export_success, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, R.string.backup_toast_export_failed, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void loadEntries() {
        if (encryptionKey != null) {
            entries = DatabaseHelper.loadDatabase(context, encryptionKey);
            entriesChanged();
        }
    }

    public void filterByTags(List<String> tags) {
        tagsFilter = tags;
        List<Entry> matchingEntries = new ArrayList<>();

        for(Entry e : entries) {
            //Entries with no tags will always be shown
            Boolean foundMatchingTag = e.getTags().isEmpty() && settings.getNoTagsToggle();

            if(settings.getTagFunctionality() == Constants.TagFunctionality.AND) {
                if(e.getTags().containsAll(tags)) {
                    foundMatchingTag = true;
                }
            } else {
                for (String tag : tags) {
                    if (e.getTags().contains(tag)) {
                        foundMatchingTag = true;
                    }
                }
            }

            if(foundMatchingTag) {
                matchingEntries.add(e);
            }
        }

        displayedEntries = sortEntries(matchingEntries);
        notifyDataSetChanged();
    }

    public void updateTimeBasedTokens() {
        boolean change = false;

        for (Entry e : entries) {
            if (e.isTimeBased()) {
                boolean item_changed = e.updateOTP();
                boolean color_changed = false;

                //Check color change only if highlighting token feature is enabled
                if(settings.isHighlightTokenOptionEnabled()) {
                    color_changed = e.hasColorChanged();
                }

                change = change || item_changed || e.hasNonDefaultPeriod() || color_changed;
            }
        }

        if (change)
            notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder entryViewHolder, int i) {
        Entry entry = displayedEntries.get(i);

        if (!entry.isTimeBased())
            entry.updateOTP();

        if(settings.isHighlightTokenOptionEnabled())
            entryViewHolder.updateColor(entry.getColor());

        entryViewHolder.updateValues(entry);

        entryViewHolder.setLabelSize(settings.getLabelSize());
        entryViewHolder.setLabelScroll(settings.getScrollLabel());

        if(settings.getThumbnailVisible())
            entryViewHolder.setThumbnailSize(settings.getThumbnailSize());
    }

    @Override @NonNull
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        int cardLayout = R.layout.component_card_default;

        Constants.CardLayouts layout = settings.getCardLayout();

        if (layout == Constants.CardLayouts.DEFAULT) {
            cardLayout = R.layout.component_card_default;
        } else if (layout == Constants.CardLayouts.COMPACT) {
            cardLayout = R.layout.component_card_compact;
        } else if (layout == Constants.CardLayouts.FULL) {
            cardLayout = R.layout.component_card_full;
        }

        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(cardLayout, viewGroup, false);

        EntryViewHolder viewHolder = new EntryViewHolder(context, itemView, settings.getTapToReveal());
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
            public void onCopyButtonClicked(String text, int position) {
                copyToClipboard(text);
                updateLastUsedAndFrequency(position, getRealIndex(position));
                if(context != null && settings.isMinimizeAppOnCopyEnabled()) {
                    ((MainActivity)context).moveTaskToBack(true);
                }
            }

            @Override
            public void onCardClicked(final int position) {
                if (settings.getTapToReveal()) {
                    final Entry entry = displayedEntries.get(position);
                    final int realIndex = entries.indexOf(entry);

                    if (entry.isVisible()) {
                        hideEntry(entry);
                    } else {
                        entries.get(realIndex).setHideTask(new Runnable() {
                            @Override
                            public void run() {
                                hideEntry(entry);
                            }
                        });
                        taskHandler.postDelayed(entries.get(realIndex).getHideTask(), settings.getTapToRevealTimeout() * 1000);

                        if (entry.isCounterBased()) {
                            updateEntry(entry, entries.get(realIndex), position);
                        }
                        entry.setVisible(true);
                        notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onCounterClicked(int position) {
                updateEntry(displayedEntries.get(position), entries.get(getRealIndex(position)), position);
            }

            @Override
            public void onCounterLongPressed(int position) {
                setCounter(position);
            }
        });

        return viewHolder;
    }

    private void updateEntry(Entry entry, Entry realEntry, final int position) {
        long counter = entry.getCounter() + 1;

        entry.setCounter(counter);
        entry.updateOTP();
        notifyItemChanged(position);

        realEntry.setCounter(counter);
        realEntry.updateOTP();
        
        saveEntries(settings.getAutoBackupEncryptedFullEnabled());
    }

    private void hideEntry(Entry entry) {
        int pos = displayedEntries.indexOf(entry);
        int realIndex = entries.indexOf(entry);

        if (realIndex >= 0) {
            entries.get(realIndex).setVisible(false);
            taskHandler.removeCallbacks(entries.get(realIndex).getHideTask());
            entries.get(realIndex).setHideTask(null);
        }

        boolean updateNeeded = updateLastUsedAndFrequency(pos, realIndex);

        if (pos >= 0) {
            displayedEntries.get(pos).setVisible(false);

            if (updateNeeded)
                notifyItemChanged(pos);
        }
    }

    private void setCounter(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        int marginSmall = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_small);
        int marginMedium = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_medium);

        final EditText input = new EditText(context);
        input.setLayoutParams(new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setText(String.format(Locale.ENGLISH, "%d", displayedEntries.get(pos).getCounter()));
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setSingleLine();

        FrameLayout container = new FrameLayout(context);
        container.setPaddingRelative(marginMedium, marginSmall, marginMedium, 0);
        container.addView(input);

        builder.setTitle(R.string.dialog_title_counter)
                .setView(container)
                .setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int realIndex = getRealIndex(pos);
                        long newCounter = Long.parseLong(input.getEditableText().toString());

                        displayedEntries.get(pos).setCounter(newCounter);
                        notifyItemChanged(pos);

                        Entry e = entries.get(realIndex);
                        e.setCounter(newCounter);

                        saveEntries(settings.getAutoBackupEncryptedFullEnabled());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create()
                .show();
    }

    private boolean updateLastUsedAndFrequency(int position, int realIndex) {
        long timeStamp = System.currentTimeMillis();
        long entryUsedFrequency = entries.get(realIndex).getUsedFrequency();

        if (position >= 0) {
            long displayEntryUsedFrequency = displayedEntries.get(position).getUsedFrequency();
            displayedEntries.get(position).setLastUsed(timeStamp);
            displayedEntries.get(position).setUsedFrequency(displayEntryUsedFrequency + 1);
        }

        entries.get(realIndex).setLastUsed(timeStamp);
        entries.get(realIndex).setUsedFrequency(entryUsedFrequency + 1);
        saveEntries(settings.getAutoBackupEncryptedFullEnabled());

        if (sortMode == SortMode.LAST_USED) {
            displayedEntries = sortEntries(displayedEntries);
            notifyDataSetChanged();
            return false;
        } else if (sortMode == SortMode.MOST_USED) {
            displayedEntries = sortEntries(displayedEntries);
            notifyDataSetChanged();
            return false;
        }

        return true;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (sortMode == SortMode.UNSORTED && displayedEntries.equals(entries)) {
            Collections.swap(entries, fromPosition, toPosition);

            displayedEntries = new ArrayList<>(entries);
            notifyItemMoved(fromPosition, toPosition);

            saveEntries(settings.getAutoBackupEncryptedFullEnabled());
        }

        return true;
    }

    public void editEntryIssuer(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        int marginSmall = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_small);
        int marginMedium = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_medium);

        final EditText input = new EditText(context);
        input.setLayoutParams(new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setText(displayedEntries.get(pos).getIssuer());
        input.setSingleLine();

        FrameLayout container = new FrameLayout(context);
        container.setPaddingRelative(marginMedium, marginSmall, marginMedium, 0);
        container.addView(input);

        builder.setTitle(R.string.dialog_title_rename)
                .setView(container)
                .setPositiveButton(R.string.button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int realIndex = getRealIndex(pos);
                        String newIssuer = input.getEditableText().toString();

                        displayedEntries.get(pos).setIssuer(newIssuer);
                        if (sortMode == SortMode.ISSUER) {
                            displayedEntries = sortEntries(displayedEntries);
                            notifyDataSetChanged();
                        } else {
                            notifyItemChanged(pos);
                        }

                        Entry e = entries.get(realIndex);
                        e.setIssuer(newIssuer);

                        DatabaseHelper.saveDatabase(context, entries, encryptionKey);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create()
                .show();
    }

    public void editEntryLabel(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        int marginSmall = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_small);
        int marginMedium = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_medium);

        final EditText input = new EditText(context);
        input.setLayoutParams(new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setText(displayedEntries.get(pos).getLabel());
        input.setSingleLine();

        FrameLayout container = new FrameLayout(context);
        container.setPaddingRelative(marginMedium, marginSmall, marginMedium, 0);
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

                        Entry e = entries.get(realIndex);
                        e.setLabel(newLabel);

                        saveEntries(settings.getAutoBackupEncryptedFullEnabled());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create()
                .show();
    }

    public void changeThumbnail(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        int marginSmall = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_small);
        int marginMedium = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_medium);

        int realIndex = getRealIndex(pos);
        final ThumbnailSelectionAdapter thumbnailAdapter = new ThumbnailSelectionAdapter(context, entries.get(realIndex).getIssuer(), entries.get(realIndex).getLabel());

        final EditText input = new EditText(context);
        input.setLayoutParams(new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        input.setSingleLine();

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                thumbnailAdapter.filter(editable.toString());
            }
        });

        int gridPadding = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_small);
        int gridBackground = Tools.getThemeColor(context, R.attr.thumbnailBackground);

        GridView grid = new GridView(context);
        grid.setAdapter(thumbnailAdapter);
        grid.setBackgroundColor(gridBackground);
        grid.setPadding(gridPadding, gridPadding, gridPadding, gridPadding);
        grid.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        int thumbnailSize = settings.getThumbnailSize();
        grid.setColumnWidth(thumbnailSize);
        grid.setNumColumns(GridView.AUTO_FIT);
        grid.setVerticalSpacing(context.getResources().getDimensionPixelSize(R.dimen.activity_margin_medium));
        grid.setHorizontalSpacing(context.getResources().getDimensionPixelSize(R.dimen.activity_margin_medium));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.addView(input);
        layout.addView(grid);

        FrameLayout container = new FrameLayout(context);
        container.setPaddingRelative(marginMedium, marginSmall, marginMedium, 0);
        container.addView(layout);

        final AlertDialog alert = builder.setTitle(R.string.menu_popup_change_image)
                .setView(container)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create();

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int realIndex = getRealIndex(pos);
                EntryThumbnail.EntryThumbnails thumbnail = EntryThumbnail.EntryThumbnails.Default;
                try {
                    int realPos = thumbnailAdapter.getRealIndex(position);
                    thumbnail = EntryThumbnail.EntryThumbnails.values()[realPos];
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Entry e = entries.get(realIndex);
                e.setThumbnail(thumbnail);

                saveEntries(settings.getAutoBackupEncryptedFullEnabled());
                notifyItemChanged(pos);
                alert.cancel();
            }
        });

        alert.show();
    }


    public void editEntryTags(final int pos) {
        final int realPos = getRealIndex(pos);
        final Entry entry = entries.get(realPos);

        HashMap<String, Boolean> tagsHashMap = new HashMap<>();
        for(String tag: entry.getTags()) {
            tagsHashMap.put(tag, true);
        }
        for(String tag: getTags()) {
            if(!tagsHashMap.containsKey(tag))
                tagsHashMap.put(tag, false);
        }
        final TagsAdapter tagsAdapter = new TagsAdapter(context, tagsHashMap);

        final Callable tagsCallable = new Callable() {
            @Override
            public Object call() throws Exception {
                entries.get(realPos).setTags(tagsAdapter.getActiveTags());
                saveEntries(settings.getAutoBackupEncryptedFullEnabled());

                List<String> inUseTags = getTags();

                HashMap<String, Boolean> tagsHashMap = new HashMap<>();
                for(String tag: tagsFilterAdapter.getTags()) {
                    if(inUseTags.contains(tag))
                        tagsHashMap.put(tag, false);
                }
                for(String tag: tagsFilterAdapter.getActiveTags()) {
                    if(inUseTags.contains(tag))
                        tagsHashMap.put(tag, true);
                }
                for(String tag: getTags()) {
                    if(inUseTags.contains(tag))
                        if(!tagsHashMap.containsKey(tag))
                            tagsHashMap.put(tag, true);
                }

                tagsFilterAdapter.setTags(tagsHashMap);
                filterByTags(tagsFilterAdapter.getActiveTags());
                return null;
            }
        };

        TagsDialog.show(context, tagsAdapter, tagsCallable, tagsCallable);
    }

    public void removeItem(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String label = displayedEntries.get(pos).getLabel();
        String message = context.getString(R.string.dialog_msg_confirm_delete, label);

        builder.setTitle(R.string.dialog_title_remove)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int realIndex = getRealIndex(pos);

                        displayedEntries.remove(pos);
                        notifyItemRemoved(pos);

                        entries.remove(realIndex);
                        saveEntries(settings.getAutoBackupEncryptedFullEnabled());
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

                if (id == R.id.menu_popup_editIssuer) {
                    editEntryIssuer(pos);
                    return true;
                } else if (id == R.id.menu_popup_editLabel) {
                    editEntryLabel(pos);
                    return true;
                } else if(id == R.id.menu_popup_changeImage) {
                    changeThumbnail(pos);
                    return true;
                } else if (id == R.id.menu_popup_editTags) {
                    editEntryTags(pos);
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

    private ArrayList<Entry> sortEntries(List<Entry> unsorted) {
        ArrayList<Entry> sorted = new ArrayList<>(unsorted);

        if (sortMode == SortMode.ISSUER) {
            Collections.sort(sorted, new IssuerComparator());
        } else if (sortMode == SortMode.LABEL) {
            Collections.sort(sorted, new LabelComparator());
        } else if (sortMode == SortMode.LAST_USED) {
            Collections.sort(sorted, new LastUsedComparator());
        } else if (sortMode == SortMode.MOST_USED) {
            Collections.sort(sorted, new MostUsedComparator());
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

    public void clearFilter() {
        if (filter != null)
            filter = null;
    }

    public List<String> getTags() {
        HashSet<String> tags = new HashSet<String>();

        for(Entry entry : entries) {
            tags.addAll(entry.getTags());
        }

        return new ArrayList<String>(tags);
    }

    public class EntryFilter extends Filter {
        private List<Constants.SearchIncludes> filterValues = settings.getSearchValues();

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            final FilterResults filterResults = new FilterResults();

            ArrayList<Entry> filtered = new ArrayList<>();
            if (constraint != null && constraint.length() != 0){
                for (int i = 0; i < entries.size(); i++) {
                    if (filterValues.contains(Constants.SearchIncludes.LABEL) && entries.get(i).getLabel().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filtered.add(entries.get(i));
                    } else if (filterValues.contains(Constants.SearchIncludes.ISSUER) && entries.get(i).getIssuer().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filtered.add(entries.get(i));
                    } else if (filterValues.contains(Constants.SearchIncludes.TAGS)) {
                        List<String> tags = entries.get(i).getTags();
                        for (int j = 0; j < tags.size(); j++) {
                            if (tags.get(j).toLowerCase().contains(constraint.toString().toLowerCase())) {
                                filtered.add(entries.get(i));
                                break;
                            }
                        }
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

    public class IssuerComparator implements Comparator<Entry> {
        Collator collator;

        IssuerComparator(){
            collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
        }

        @Override
        public int compare(Entry o1, Entry o2) {
            return collator.compare(o1.getIssuer(), o2.getIssuer());
        }
    }

    public class LabelComparator implements Comparator<Entry> {
        Collator collator;

        LabelComparator(){
            collator = Collator.getInstance();
            collator.setStrength(Collator.PRIMARY);
        }

        @Override
        public int compare(Entry o1, Entry o2) {
            return collator.compare(o1.getLabel(), o2.getLabel());
        }
    }

    public class LastUsedComparator implements Comparator<Entry> {
        @Override
        public int compare(Entry o1, Entry o2) {
            return Long.compare(o2.getLastUsed(), o1.getLastUsed());
        }
    }

    public class MostUsedComparator implements Comparator<Entry> {
        @Override
        public int compare(Entry o1, Entry o2) {
            return Long.compare(o2.getUsedFrequency(), o1.getUsedFrequency());
        }
    }

    public interface Callback {
        void onMoveEventStart();
        void onMoveEventStop();
    }
}
