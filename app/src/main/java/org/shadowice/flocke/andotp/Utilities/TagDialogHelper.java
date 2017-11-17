package org.shadowice.flocke.andotp.Utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.View.TagsAdapter;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class TagDialogHelper {
    public static void createTagsDialog(Context context, final TagsAdapter tagsAdapter, final Callable newTagCallable, final Callable selectedTagsCallable) {
        final EditText input = new EditText(context);

        final AlertDialog.Builder newTagBuilder = new AlertDialog.Builder(context);
        newTagBuilder.setTitle(R.string.button_new_tag)
                .setView(input)
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
        tagsSelectionView.setAdapter(tagsAdapter);
        tagsSelectionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckedTextView checkedTextView = ((CheckedTextView)view);
                checkedTextView.setChecked(!checkedTextView.isChecked());

                tagsAdapter.setTagState(checkedTextView.getText().toString(), checkedTextView.isChecked());
            }
        });

        final AlertDialog.Builder tagsSelectorBuilder = new AlertDialog.Builder(context);
        tagsSelectorBuilder.setTitle(R.string.label_tags)
                .setView(tagsSelectionView)
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
