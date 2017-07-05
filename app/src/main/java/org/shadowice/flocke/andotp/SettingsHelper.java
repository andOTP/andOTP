/*
 * Copyright (C) 2017 Jakob Nixdorf
 * Copyright (C) 2015 Bruno Bierbaumer
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

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import static org.shadowice.flocke.andotp.Utils.readFully;
import static org.shadowice.flocke.andotp.Utils.writeFully;

public class SettingsHelper {
    public static final String KEY_FILE = "otp.key";
    public static final String SETTINGS_FILE = "secrets.dat";

    public static void store(Context context, ArrayList<Entry> entries) {
        JSONArray a = new JSONArray();

        for(Entry e: entries){
            try {
                a.put(e.toJSON());
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        try {
            byte[] data = a.toString().getBytes();

            SecretKey key = EncryptionHelper.loadOrGenerateKeys(context, new File(context.getFilesDir() + "/" + KEY_FILE));
            data = EncryptionHelper.encrypt(key,data);

            writeFully(new File(context.getFilesDir() + "/" + SETTINGS_FILE), data);

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static ArrayList<Entry> load(Context context){
        ArrayList<Entry> entries = new ArrayList<>();

        try {
            byte[] data = readFully(new File(context.getFilesDir() + "/" + SETTINGS_FILE));

            SecretKey key = EncryptionHelper.loadOrGenerateKeys(context, new File(context.getFilesDir() + "/" + KEY_FILE));
            data = EncryptionHelper.decrypt(key, data);

            JSONArray json = new JSONArray(new String(data));

            for (int i = 0; i < json.length(); i++) {
                entries.add(new Entry(json.getJSONObject(i)));
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return entries;
    }

    public static boolean exportAsJSON(Context context, Uri file) {
        ArrayList<Entry> entries = load(context);

        JSONArray json = new JSONArray();

        for(Entry e: entries){
            try {
                json.put(e.toJSON());
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        return FileHelper.writeStringToFile(context, file, json.toString());
    }

    public static boolean importFromJSON(Context context, Uri file) {
        boolean success = true;

        String content = FileHelper.readFileToString(context, file);
        ArrayList<Entry> entries = new ArrayList<>();

        try {
            JSONArray json = new JSONArray(content);

            for (int i = 0; i < json.length(); i++) {
                entries.add(new Entry(json.getJSONObject(i)));
            }
        } catch (Exception error) {
            success = false;
            error.printStackTrace();
        }

        store(context, entries);

        return success;
    }
}