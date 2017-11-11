package org.shadowice.flocke.andotp.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter ;
import android.widget.CheckedTextView;

import org.shadowice.flocke.andotp.Utilities.Settings;

import java.util.List;

public class TagsAdapter extends ArrayAdapter<String> {
    private Context context;
    private Settings settings;
    private List<String> tags;
    private static final int layoutResourceId = android.R.layout.simple_list_item_multiple_choice;

    public TagsAdapter(Context context, List<String> tags) {
        super(context, layoutResourceId, tags);
        this.context = context;
        this.settings = new Settings(context);
        this.tags = tags;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        CheckedTextView checkedTextView;
        if (view == null) {
            checkedTextView = (CheckedTextView)LayoutInflater.from(context).inflate(layoutResourceId, viewGroup, false);
        } else{
            checkedTextView = (CheckedTextView) view;
        }
        checkedTextView.setText(tags.get(i));
        checkedTextView.setChecked(settings.getTagToggle(tags.get(i)));
        return checkedTextView;
    }
}
