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

package org.shadowice.flocke.andotp.View;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.Utilities.Settings;

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
        notifyDataSetChanged();
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

    public static HashMap<String, Boolean> createTagsMap(ArrayList<Entry> entries, Settings settings) {
        HashMap<String, Boolean> tagsHashMap = new HashMap<>();

        for(Entry entry : entries) {
            for(String tag : entry.getTags())
                tagsHashMap.put(tag, settings.getTagToggle(tag));
        }

        return tagsHashMap;
    }
}
