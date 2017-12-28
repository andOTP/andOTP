/*
 * Copyright (C) 2017 Jakob Nixdorf
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

package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.Locale;

public class Tools {
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /* Get a color based on the current theme */
    public static int getThemeColor(Context context, int colorAttr) {
        Resources.Theme theme = context.getTheme();
        TypedArray arr = theme.obtainStyledAttributes(new int[]{colorAttr});

        int colorValue = arr.getColor(0, -1);
        arr.recycle();

        return colorValue;
    }

    /* Create a ColorFilter based on the current theme */
    public static ColorFilter getThemeColorFilter(Context context, int colorAttr) {
        return new PorterDuffColorFilter(getThemeColor(context, colorAttr), PorterDuff.Mode.SRC_IN);
    }

    public static Uri buildUri(String base, String name) {
        return Uri.fromFile(new File(base, name));
    }

    public static boolean mkdir(String path) {
        File dir = new File(path);
        return dir.exists() || dir.mkdirs();
    }

    public static Locale getSystemLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            return Resources.getSystem().getConfiguration().locale;
        }
    }

    public static String formatToken(String s, int chunkSize) {
        StringBuilder ret = new StringBuilder("");
        final int len = s.length();
        int index = len;
        while (index > 0) {
            ret.insert(0, s.substring(Math.max(index - chunkSize, 0), index));
            ret.insert(0, " ");
            index = index - chunkSize;
        }
        return ret.toString().trim();
    }
}
