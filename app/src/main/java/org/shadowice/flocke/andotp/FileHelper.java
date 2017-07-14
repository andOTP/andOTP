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

package org.shadowice.flocke.andotp;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FileHelper {
    public static String readFileToString(Context context, Uri file) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
            inputStream.close();
        } catch (Exception error) {
            error.printStackTrace();
        }

        return(stringBuilder.toString());
    }

    public static boolean writeStringToFile(Context context, Uri file, String content) {
        boolean success = true;

        try {
            OutputStream outputStream = context.getContentResolver().openOutputStream(file);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(content);
            writer.close();
            outputStream.close();
        } catch (Exception error) {
            success = false;
            error.printStackTrace();
        }

        return success;
    }
}

