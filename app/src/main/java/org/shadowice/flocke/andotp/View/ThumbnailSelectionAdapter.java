package org.shadowice.flocke.andotp.View;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.EntryThumbnail;

import java.util.ArrayList;

public class ThumbnailSelectionAdapter extends ArrayAdapter<EntryThumbnail.EntryThumbnails> {
    private Context context;

    public ThumbnailSelectionAdapter(Context context) {
        super(context, R.layout.component_thumbnail_selection, new ArrayList<EntryThumbnail.EntryThumbnails>());
        this.context = context;

        for (EntryThumbnail.EntryThumbnails thumb : EntryThumbnail.EntryThumbnails.values()) {
            add(thumb);
        }
    }

    @NonNull
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        View newView;
        if (view == null) {
            newView = LayoutInflater.from(context).inflate(R.layout.component_thumbnail_selection , viewGroup, false);
        } else{
            newView = view;
        }

        ImageView imageView = (ImageView) newView.findViewById(R.id.thumbnail_selection_icon);
        TextView textView = (TextView) newView.findViewById(R.id.thumbnail_selection_text);

        EntryThumbnail.EntryThumbnails thumb = getItem(i);

        int size = context.getResources().getDimensionPixelSize(R.dimen.card_thumbnail_size);
        imageView.setImageBitmap(EntryThumbnail.getThumbnailGraphic(context, thumb.name(), size, thumb));
        textView.setText(thumb.name());

        return newView;
    }
}
