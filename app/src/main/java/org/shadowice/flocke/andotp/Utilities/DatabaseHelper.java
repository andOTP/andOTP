/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
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

import android.app.backup.BackupManager;
import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.crypto.SecretKey;

public class DatabaseHelper {

    static final Object DatabaseFileLock = new Object();

    public static void wipeDatabase(Context context) {
        File db = new File(context.getFilesDir() + "/" + Constants.FILENAME_DATABASE);
        File dbBackup = new File(context.getFilesDir() + "/" + Constants.FILENAME_DATABASE_BACKUP);
        db.delete();
        dbBackup.delete();
    }

    private static void copyFile(File src, File dst)
        throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
        }
    }

    public static boolean backupDatabase(Context context) {
        File original = new File(context.getFilesDir() + "/" + Constants.FILENAME_DATABASE);
        File backup = new File(context.getFilesDir() + "/" + Constants.FILENAME_DATABASE_BACKUP);

        if (original.exists()) {
            try {
                copyFile(original, backup);
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    public static boolean restoreDatabaseBackup(Context context) {
        File original = new File(context.getFilesDir() + "/" + Constants.FILENAME_DATABASE);
        File backup = new File(context.getFilesDir() + "/" + Constants.FILENAME_DATABASE_BACKUP);

        if (backup.exists()) {
            try {
                copyFile(backup, original);
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    /* Database functions */
    public static boolean saveDatabase(Context context, ArrayList<Entry> entries, SecretKey encryptionKey) {
        if (encryptionKey == null) {
            Toast.makeText(context, R.string.toast_encryption_key_empty, Toast.LENGTH_LONG).show();
            return false;
        }

        String jsonString = entriesToString(entries);

        try {
            synchronized (DatabaseHelper.DatabaseFileLock) {
                byte[] data = EncryptionHelper.encrypt(encryptionKey, jsonString.getBytes());

                FileHelper.writeBytesToFile(new File(context.getFilesDir() + "/" + Constants.FILENAME_DATABASE), data);
            }
        } catch (Exception error) {
            error.printStackTrace();
            return false;
        }

        BackupManager backupManager = new BackupManager(context);
        backupManager.dataChanged();

        return true;
    }

    public static ArrayList<Entry> loadDatabase(Context context, SecretKey encryptionKey) {
        ArrayList<Entry> entries = new ArrayList<>();

        if (encryptionKey != null) {
            try {
                synchronized (DatabaseHelper.DatabaseFileLock) {
                    byte[] data = FileHelper.readFileToBytes(new File(context.getFilesDir() + "/" + Constants.FILENAME_DATABASE));
                    data = EncryptionHelper.decrypt(encryptionKey, data);

                    entries = stringToEntries(new String(data));
                }
            } catch (Exception error) {
                error.printStackTrace();
            }
        } else {
            Toast.makeText(context, R.string.toast_encryption_key_empty, Toast.LENGTH_LONG).show();
        }

        return entries;
    }

    /* Conversion functions */

    public static String entriesToString(ArrayList<Entry> entries) {
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

    public static ArrayList<Entry> stringToEntries(String data) {
        ArrayList<Entry> entries = new ArrayList<>();

        try {
            JSONArray json = new JSONArray(data);

            for (int i = 0; i < json.length(); i++) {
                Entry entry = new Entry(json.getJSONObject(i));
                entries.add(entry);
            }
        } catch (Exception error) {
            error.printStackTrace();
        }

        return entries;
    }
}