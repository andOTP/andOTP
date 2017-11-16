package org.shadowice.flocke.andotp.View;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TagsAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> tagsOrder;
    private HashMap<String, Boolean> tagsState;
    private static final int layoutResourceId = android.R.layout.simple_list_item_multiple_choice;

    public TagsAdapter(Context context, HashMap<String, Boolean> tags) {
        super(context, layoutResourceId, new ArrayList<>(tags.keySet()));
        this.context = context;

        this.tagsState = tags;
        this.tagsOrder = new ArrayList<>(tagsState.keySet());
        Collections.sort(this.tagsOrder);
    }

    @NonNull
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        CheckedTextView checkedTextView;
        if (view == null) {
            checkedTextView = (CheckedTextView)LayoutInflater.from(context).inflate(layoutResourceId, viewGroup, false);
        } else{
            checkedTextView = (CheckedTextView) view;
        }
        checkedTextView.setText(tagsOrder.get(i));
        checkedTextView.setChecked(tagsState.get(tagsOrder.get(i)));

        return checkedTextView;
    }

    public List<String> getTags() {
        return tagsOrder;
    }

    public Boolean getTagState(String tag) {
        if(tagsState.containsKey(tag))
            return tagsState.get(tag);
        return false;
    }

    public void setTagState(String tag, Boolean state) {
        if(tagsState.containsKey(tag))
            tagsState.put(tag, state);
    }

    public List<String> getActiveTags() {
        List<String> tagsList = new ArrayList<>();
        for(String tag : tagsOrder)
        {
            if(tagsState.get(tag)) {
                tagsList.add(tag);
            }
        }
        return tagsList;
    }

    public boolean allTagsActive() {
        for (String key : tagsState.keySet())
            if (! tagsState.get(key))
                return false;

        return true;
    }

    public HashMap<String, Boolean> getTagsWithState() {
        return new HashMap<String, Boolean>(tagsState);
    }

    public void setTags(HashMap<String, Boolean> tags) {
        this.tagsState = tags;
        this.tagsOrder = new ArrayList<>(tagsState.keySet());
        Collections.sort(this.tagsOrder);

        this.clear();
        this.addAll(getTags());
        notifyDataSetChanged();
    }
}
