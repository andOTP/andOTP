package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.shadowice.flocke.andotp.R;

public class EntryThumbnail {
    public enum EntryThumbnails {
        Default
    }

    public static Bitmap getThumbnailGraphic(Context context, String label, int size, EntryThumbnails thumbnail) {
        switch (thumbnail) {
            case Default:
                LetterBitmap letterBitmap = new LetterBitmap(context);
                return letterBitmap.getLetterTile(label, label, size, size);
            default:
                return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        }
    }
}
