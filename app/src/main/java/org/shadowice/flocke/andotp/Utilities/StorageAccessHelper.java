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

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import org.apache.commons.codec.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StorageAccessHelper {
    public static boolean saveFile(Context context, Uri file, byte[] data) {
        boolean success = true;

        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(file, "w");
            FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());

            fileOutputStream.write(data);

            fileOutputStream.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    public static boolean saveFile(Context context, Uri file, String data) {
        return saveFile(context, file, data.getBytes(Charsets.UTF_8));
    }

    public static byte[] loadFile(Context context, Uri file) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(file)) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int count;

            while ((count = inputStream.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }

            return bytes.toByteArray();
        }
    }

    public static String loadFileString(Context context, Uri file) {
        String result = "";

        try {
            byte[] content = loadFile(context, file);
            result = new String(content, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
