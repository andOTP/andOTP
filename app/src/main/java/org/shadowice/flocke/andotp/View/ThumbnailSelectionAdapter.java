package org.shadowice.flocke.andotp.View;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.EntryThumbnail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThumbnailSelectionAdapter extends BaseAdapter {
    private Context context;
    private List items;
    private String label = "Example";

    ThumbnailSelectionAdapter(Context context, String label) {
        items = new ArrayList(EntryThumbnail.EntryThumbnails.values().length);
        Collections.addAll(items, EntryThumbnail.EntryThumbnails.values());
        this.label = label;
        this.context = context;
    }

    void filter(String filter) {
        items.clear();
        for (EntryThumbnail.EntryThumbnails thumb : EntryThumbnail.EntryThumbnails.values()) {
            if(thumb.name().toLowerCase().contains(filter.toLowerCase())) {
                items.add(thumb);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        if(i < getCount())
            return items.get(i);
        else
            return EntryThumbnail.EntryThumbnails.Default;
    }

    @Override
    public long getItemId(int i) {
        return ((EntryThumbnail.EntryThumbnails) getItem(i)).ordinal();
    }

    @NonNull
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        int size = context.getResources().getDimensionPixelSize(R.dimen.card_thumbnail_size);
        ImageView imageView;
        if (view == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(size, size));
        } else {
            imageView = (ImageView) view;
        }

        EntryThumbnail.EntryThumbnails thumb = (EntryThumbnail.EntryThumbnails)getItem(i);

        imageView.setImageBitmap(EntryThumbnail.getThumbnailGraphic(context, label, size, thumb));
        return imageView;
    }
}
