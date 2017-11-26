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

public class ThumbnailSelectionAdapter extends BaseAdapter {
    private Context context;

    ThumbnailSelectionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return EntryThumbnail.EntryThumbnails.values().length;
    }

    @Override
    public Object getItem(int i) {
        if(i >= EntryThumbnail.EntryThumbnails.values().length)
            return EntryThumbnail.EntryThumbnails.Default;
        else
            return EntryThumbnail.EntryThumbnails.values()[i];
    }

    @Override
    public long getItemId(int i) {
        return EntryThumbnail.EntryThumbnails.values()[i].ordinal();
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

        imageView.setImageBitmap(EntryThumbnail.getThumbnailGraphic(context, thumb.name(), size, thumb));
        return imageView;
    }
}
