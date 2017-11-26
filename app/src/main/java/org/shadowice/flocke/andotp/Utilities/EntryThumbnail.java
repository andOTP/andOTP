package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import org.shadowice.flocke.andotp.R;

public class EntryThumbnail {
    public enum EntryThumbnails {
        Default(R.mipmap.ic_launcher_round),
        Github(R.drawable.ic_github);

        private int resource;

        EntryThumbnails(int resource) {
            this.resource = resource;
        }

        public int getResource() {
            return resource;
        }
    }

    public static Bitmap getThumbnailGraphic(Context context, String label, int size, EntryThumbnails thumbnail) {
        if(thumbnail == EntryThumbnails.Default) {
            LetterBitmap letterBitmap = new LetterBitmap(context);
            return letterBitmap.getLetterTile(label, label, size, size);
        }

        try {
            Drawable drawable = context.getResources().getDrawable(thumbnail.getResource());
            Bitmap bitmap = Bitmap.createBitmap(drawable.getMinimumWidth(), drawable.getMinimumHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);
    }
}
