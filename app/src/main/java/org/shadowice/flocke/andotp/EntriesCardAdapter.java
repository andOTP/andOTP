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
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.shadowice.flocke.andotp.ItemTouchHelper.ItemTouchHelperAdapter;
import org.shadowice.flocke.andotp.ItemTouchHelper.ItemTouchHelperViewHolder;

import java.util.ArrayList;
import java.util.Collections;

public class EntriesCardAdapter extends RecyclerView.Adapter<EntriesCardAdapter.EntryViewHolder>
    implements ItemTouchHelperAdapter {

    private Context context;
    private ArrayList<Entry> entries;
    public ViewHolderEventCallback viewHolderEventCallback;

    public EntriesCardAdapter(Context context, ArrayList<Entry> entries) {
        this.context = context;
        this.entries = entries;
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public Entry getItem(int i) {
        return entries.get(i);
    }

    public void setEntries(ArrayList<Entry> e) {
        entries = e;
    }

    @Override
    public void onBindViewHolder(EntryViewHolder entryViewHolder, int i) {
        Entry entry = entries.get(i);

        entryViewHolder.OTPValue.setText(entry.getCurrentOTP());
        entryViewHolder.OTPLabel.setText(entry.getLabel());
        entryViewHolder.eventCallback = viewHolderEventCallback;

        if (entry.hasNonDefaultPeriod()) {
            entryViewHolder.customPeriodLayout.setVisibility(View.VISIBLE);
            entryViewHolder.customPeriod.setText(String.format(context.getString(R.string.format_custom_period), entry.getPeriod()));
        }
    }

    @Override
    public EntryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);

        return new EntryViewHolder(itemView);
    }

    @Override
    public void onItemDismiss(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(context.getString(R.string.alert_remove))
                .setMessage(context.getString(R.string.msg_confirm_delete))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        entries.remove(position);
                        notifyItemRemoved(position);

                        SettingsHelper.store(context, entries);
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
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(entries, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(entries, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

        SettingsHelper.store(context, entries);

        return true;
    }

    public void setMoveEventCallback(ViewHolderEventCallback cb) {
        this.viewHolderEventCallback = cb;
    }

    public static class EntryViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperViewHolder {

        private ViewHolderEventCallback eventCallback;

        protected TextView OTPValue;
        protected TextView OTPLabel;
        protected ImageView editButton;
        protected LinearLayout customPeriodLayout;
        protected TextView customPeriod;

        public EntryViewHolder(View v) {
            super(v);

            OTPValue = (TextView) v.findViewById(R.id.textViewOTP);
            OTPLabel = (TextView) v.findViewById(R.id.textViewLabel);
            editButton = (ImageView) v.findViewById(R.id.editImage);
            customPeriodLayout = (LinearLayout) v.findViewById(R.id.customPeriodLayout);
            customPeriod = (TextView) v.findViewById(R.id.customPeriod);

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    eventCallback.onEditButtonClicked(getAdapterPosition());
                }
            });
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

        void onEditButtonClicked(int pos);
    }
}
