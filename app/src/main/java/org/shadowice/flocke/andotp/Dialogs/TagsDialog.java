/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
 * Copyright (C) 2017-2020 Richy HBM
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

package org.shadowice.flocke.andotp.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.View.TagsAdapter;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class TagsDialog {
    public static void show(Context context, final TagsAdapter tagsAdapter, final Callable newTagCallable, final Callable selectedTagsCallable) {
        int margin = context.getResources().getDimensionPixelSize(R.dimen.activity_margin);
        int marginSmall = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_small);
        int marginMedium = context.getResources().getDimensionPixelSize(R.dimen.activity_margin_medium);

        final EditText input = new EditText(context);
        input.setLayoutParams(new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final FrameLayout inputLayout = new FrameLayout(context);
        inputLayout.setPaddingRelative(marginMedium, marginSmall, marginMedium, 0);
        inputLayout.addView(input);

        final AlertDialog.Builder newTagBuilder = new AlertDialog.Builder(context);
        newTagBuilder.setTitle(R.string.button_new_tag)
                .setView(inputLayout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newTag = input.getText().toString();
                        HashMap<String, Boolean> allTags = tagsAdapter.getTagsWithState();
                        allTags.put(newTag, true);
                        tagsAdapter.setTags(allTags);
                        if(newTagCallable != null) {
                            try {
                                newTagCallable.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });

        final ListView tagsSelectionView = new ListView(context);
        tagsSelectionView.setDivider(null);
        tagsSelectionView.setLayoutParams(new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tagsSelectionView.setAdapter(tagsAdapter);
        tagsSelectionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = ((CheckedTextView)view);
                checkedTextView.setChecked(!checkedTextView.isChecked());

                tagsAdapter.setTagState(checkedTextView.getText().toString(), checkedTextView.isChecked());
            }
        });

        final FrameLayout tagsSelectionLayout = new FrameLayout(context);
        tagsSelectionLayout.setPaddingRelative(margin, marginSmall, margin, 0);
        tagsSelectionLayout.addView(tagsSelectionView);

        final AlertDialog.Builder tagsSelectorBuilder = new AlertDialog.Builder(context);
        tagsSelectorBuilder.setTitle(R.string.label_tags)
                .setView(tagsSelectionLayout)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(selectedTagsCallable != null) {
                            try {
                                selectedTagsCallable.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setNeutralButton(R.string.button_new_tag, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        newTagBuilder.create().show();
                    }
                }).create().show();

    }
}
