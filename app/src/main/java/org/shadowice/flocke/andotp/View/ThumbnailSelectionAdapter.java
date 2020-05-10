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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.shadowice.flocke.andotp.Utilities.EntryThumbnail;
import org.shadowice.flocke.andotp.Utilities.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThumbnailSelectionAdapter extends BaseAdapter {
    private Context context;
    private List items;
    private String issuer = "Example";
    private String label = "Example";
    private Settings settings;

    ThumbnailSelectionAdapter(Context context, String issuer, String label) {
        items = new ArrayList(EntryThumbnail.EntryThumbnails.values().length);
        Collections.addAll(items, EntryThumbnail.EntryThumbnails.values());
        this.issuer = issuer;
        this.label = label;
        this.context = context;
        settings = new Settings(context);
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

    public int getRealIndex(int displayPosition) {
        return ((EntryThumbnail.EntryThumbnails)getItem(displayPosition)).ordinal();
    }

    @Override
    public long getItemId(int i) {
        return ((EntryThumbnail.EntryThumbnails) getItem(i)).ordinal();
    }

    @NonNull
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {
        int thumbnailSize = settings.getThumbnailSize();
        ImageView imageView;
        if (view == null) {
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(thumbnailSize, thumbnailSize));
        } else {
            imageView = (ImageView) view;
        }

        EntryThumbnail.EntryThumbnails thumb = (EntryThumbnail.EntryThumbnails)getItem(i);

        imageView.setImageBitmap(EntryThumbnail.getThumbnailGraphic(context, issuer, label, thumbnailSize, thumb));
        return imageView;
    }
}
