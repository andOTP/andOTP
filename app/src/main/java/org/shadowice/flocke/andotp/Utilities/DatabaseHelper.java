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

package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.shadowice.flocke.andotp.Database.Entry;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import javax.crypto.SecretKey;

public class DatabaseHelper {
    public static final String KEY_FILE = "otp.key";
    public static final String SETTINGS_FILE = "secrets.dat";

    /* Database functions */

    public static void saveDatabase(Context context, ArrayList<Entry> entries) {
        Settings settings = new Settings(context);

        String plainKeys = settings.getBackupAsUriKeyFormat() ?
                DatabaseHelper.entriesToURIKeyString(entries) :
                DatabaseHelper.entriesToJsonString(entries);

        try {
            byte[] data = plainKeys.getBytes();

            SecretKey key = KeyStoreHelper.loadOrGenerateWrappedKey(context, new File(context.getFilesDir() + "/" + KEY_FILE));
            data = EncryptionHelper.encrypt(key, data);

            FileHelper.writeBytesToFile(new File(context.getFilesDir() + "/" + SETTINGS_FILE), data);

        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    public static ArrayList<Entry> loadDatabase(Context context){
        ArrayList<Entry> entries = new ArrayList<>();
        Settings settings = new Settings(context);

        try {
            byte[] data = FileHelper.readFileToBytes(new File(context.getFilesDir() + "/" + SETTINGS_FILE));

            SecretKey key = KeyStoreHelper.loadOrGenerateWrappedKey(context, new File(context.getFilesDir() + "/" + KEY_FILE));
            data = EncryptionHelper.decrypt(key, data);

            entries = settings.getBackupAsUriKeyFormat() ?
                    DatabaseHelper.uriKeysToEntries(new String(data)) :
                    DatabaseHelper.jsonToEntries(new String(data));

        } catch (Exception error) {
            error.printStackTrace();
        }

        return entries;
    }

    /* Conversion functions */
    public static String entriesToURIKeyString(ArrayList<Entry> entries) {
        StringBuilder builder = new StringBuilder();

        for(Entry e: entries){
            try {
                builder.append(e.toUriKey()).append('\n');
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        return builder.toString();
    }

    public static String entriesToJsonString(ArrayList<Entry> entries) {
        JSONArray json = new JSONArray();

        for(Entry e: entries){
            try {
                json.put(e.toJSON());
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        return json.toString();
    }

    public static ArrayList<Entry> uriKeysToEntries(String data) {
        ArrayList<Entry> entries = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(data);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                entries.add(new Entry(line));
            }
            scanner.close();
        } catch (Exception error) {
            error.printStackTrace();
        }

        return entries;
    }

    public static ArrayList<Entry> jsonToEntries(String data) {
        ArrayList<Entry> entries = new ArrayList<>();

        try {
            JSONArray json = new JSONArray(data);

            for (int i = 0; i < json.length(); i++) {
                entries.add(new Entry(json.getJSONObject(i)));
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return entries;
    }

    /* Export functions */

    public static boolean exportKeys(Context context, Uri file) {
        ArrayList<Entry> entries = loadDatabase(context);

        Settings settings = new Settings(context);

        String plainKeys = settings.getBackupAsUriKeyFormat() ?
                DatabaseHelper.entriesToURIKeyString(entries) :
                DatabaseHelper.entriesToJsonString(entries);

        return FileHelper.writeStringToFile(context, file, plainKeys);
    }

    public static boolean importFromKeys(Context context, Uri file) {
        boolean success = false;
        Settings settings = new Settings(context);

        String content = FileHelper.readFileToString(context, file);

        if (! content.isEmpty()) {
            ArrayList<Entry> entries = settings.getBackupAsUriKeyFormat() ? uriKeysToEntries(content) : jsonToEntries(content);

            saveDatabase(context, entries);

            success = true;
        }

        return success;
    }
}