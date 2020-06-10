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

package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.TypedValue;

import org.shadowice.flocke.andotp.R;

/**
 * Orginal http://stackoverflow.com/questions/23122088/colored-boxed-with-letters-a-la-gmail
 * Used to create a {@link Bitmap} that contains a letter used in the English
 * alphabet or digit, if there is no letter or digit available, a default image
 * is shown instead.
 *
 * Only English language supported.
 */
class LetterBitmap {

    /**
     * The number of available tile colors
     */
    private static final int NUM_OF_TILE_COLORS = 8;

    /**
     * The {@link TextPaint} used to draw the letter onto the tile
     */
    private final TextPaint mPaint = new TextPaint();
    /**
     * The bounds that enclose the letter
     */
    private final Rect mBounds = new Rect();
    /**
     * The {@link Canvas} to draw on
     */
    private final Canvas mCanvas = new Canvas();
    /**
     * The first char of the name being displayed
     */
    private final char[] mFirstChar = new char[1];

    /**
     * The background colors of the tile
     */
    private final TypedArray mColors;
    /**
     * The font size used to display the letter
     */
    private final float mTileLetterFontSizeScale;

    /**
     * Constructor for <code>LetterTileProvider</code>
     *
     * @param context The {@link Context} to use
     */
    public LetterBitmap(Context context) {
        final Resources res = context.getResources();

        mPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD));
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setAntiAlias(true);

        mColors = res.obtainTypedArray(R.array.letter_tile_colors);

        TypedValue typedValue = new TypedValue();
        res.getValue(R.dimen.tile_letter_font_size_scale, typedValue, true);
        mTileLetterFontSizeScale = typedValue.getFloat();
    }

    /**
     * @param displayName The name used to create the letter for the tile
     * @param key         The key used to generate the background color for the tile
     * @param width       The desired width of the tile
     * @param height      The desired height of the tile
     * @return A {@link Bitmap} that contains a letter used in the English
     * alphabet or digit, if there is no letter or digit available, a
     * default image is shown instead
     */
    public Bitmap getLetterTile(String displayName, String key, int width, int height) {
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        char firstChar = '?';

        if (!displayName.isEmpty() && startsWithAlphabeticOrDigit(displayName)) {
            firstChar = displayName.charAt(0);
        }

        final Canvas c = mCanvas;
        c.setBitmap(bitmap);
        c.drawColor(pickColor(key));

        mFirstChar[0] = Character.toUpperCase(firstChar);
        mPaint.setTextSize(mTileLetterFontSizeScale * height);
        mPaint.getTextBounds(mFirstChar, 0, 1, mBounds);
        c.drawText(mFirstChar, 0, 1, width / 2, height / 2
                + (mBounds.bottom - mBounds.top) / 2, mPaint);
        return bitmap;
    }

    /**
     * @param string The string to check
     * @return True if <code>string</code> starts with an alphabetic letter or a digit,
     * false otherwise
     */
    private static boolean startsWithAlphabeticOrDigit(String string) {
        return Character.isAlphabetic(string.codePointAt(0)) ||
                Character.isDigit(string.charAt(0));
    }

    /**
     * @param key The key used to generate the tile color
     * @return A new or previously chosen color for <code>key</code> used as the
     * tile background color
     */
    private int pickColor(String key) {
        // String.hashCode() is not supposed to change across java versions, so
        // this should guarantee the same key always maps to the same color
        final int color = Math.abs(key.hashCode()) % NUM_OF_TILE_COLORS;
        try {
            return mColors.getColor(color, Color.BLACK);
        } finally {
            mColors.recycle();
        }
    }
}