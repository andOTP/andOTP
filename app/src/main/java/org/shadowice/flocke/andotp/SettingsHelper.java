/*
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
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import static android.os.Environment.getExternalStorageDirectory;
import static org.shadowice.flocke.andotp.Utils.readFully;
import static org.shadowice.flocke.andotp.Utils.writeFully;

public class SettingsHelper {
    public static final String KEY_FILE = "otp.key";
    public static final String SETTINGS_FILE = "secrets.dat";

    public static final String EXPORT_FILE = "otp_accounts.json";

    public static void store(Context context, JSONArray json) {
        try {
            byte[] data = json.toString().getBytes();

            SecretKey key = EncryptionHelper.loadOrGenerateKeys(context, new File(context.getFilesDir() + "/" + KEY_FILE));
            data = EncryptionHelper.encrypt(key,data);

            writeFully(new File(context.getFilesDir() + "/" + SETTINGS_FILE), data);

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static void store(Context context, ArrayList<Entry> entries){
        JSONArray a = new JSONArray();

        for(Entry e: entries){
            try {
                a.put(e.toJSON());
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        store(context, a);
    }

    public static ArrayList<Entry> load(Context context){
        ArrayList<Entry> entries = new ArrayList<>();

        try {
            JSONArray a = readJSON(context);

            for (int i = 0; i < a.length(); i++) {
                entries.add(new Entry(a.getJSONObject(i)));
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return entries;
    }

    public static JSONArray readJSON(Context context) {
        JSONArray json = new JSONArray();

        try {
            byte[] data = readFully(new File(context.getFilesDir() + "/" + SETTINGS_FILE));

            SecretKey key = EncryptionHelper.loadOrGenerateKeys(context, new File(context.getFilesDir() + "/" + KEY_FILE));
            data = EncryptionHelper.decrypt(key, data);

            json = new JSONArray(new String(data));
        }
        catch (Exception error) {
            error.printStackTrace();
        }

        return json;
    }

    public static boolean exportAsJSON(Context context) {
        File outputFile = new File(getExternalStorageDirectory() + "/" + EXPORT_FILE);

        JSONArray data = readJSON(context);

        boolean success = true;

        try {
            Writer output = new BufferedWriter(new FileWriter(outputFile));
            output.write(data.toString());
            output.close();
        } catch (Exception error) {
            success = false;
            error.printStackTrace();
        }

        return success;
    }

    public static boolean importFromJSON(Context context, Uri file) {
        boolean success = true;

        StringBuilder stringBuilder = new StringBuilder();

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            inputStream.close();
        } catch (Exception error) {
            success = false;
            error.printStackTrace();
        }
        String content = stringBuilder.toString();

        JSONArray json = null;

        try {
            json = new JSONArray(content);
        } catch (Exception error) {
            success = false;
            error.printStackTrace();
        }

        store(context, json);

        return success;
    }
}