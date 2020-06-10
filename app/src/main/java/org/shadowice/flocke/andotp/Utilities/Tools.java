/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import org.shadowice.flocke.andotp.R;

import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Tools {
    private final static String CSS_RGBA_FORMAT = "rgba(%1$d,%2$d,%3$d,%4$1f)";

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

    public static int getThemeResource(Context context, int styleAttr) {
        Resources.Theme theme = context.getTheme();
        TypedArray arr = theme.obtainStyledAttributes(new int[]{styleAttr});

        int styleValue = arr.getResourceId(0, -1);
        arr.recycle();

        return styleValue;
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

    public static String formatTokenString(int token, int digits) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
        numberFormat.setMinimumIntegerDigits(digits);
        numberFormat.setGroupingUsed(false);

        return numberFormat.format(token);
    }

    public static String formatToken(String s, int chunkSize) {
        if (chunkSize ==0 || s == null)
            return s;

        StringBuilder ret = new StringBuilder("");
        int index = s.length();
        while (index > 0) {
            ret.insert(0, s.substring(Math.max(index - chunkSize, 0), index));
            ret.insert(0, " ");
            index = index - chunkSize;
        }
        return ret.toString().trim();
    }

    public static String getDateTimeString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);
        Date now = Calendar.getInstance().getTime();
        return df.format(now);
    }

    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(context.getString(R.string.label_clipboard_content), text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, R.string.toast_copied_to_clipboard, Toast.LENGTH_LONG).show();
    }
}
