package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

    public static String getContentFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static boolean saveFile(Context context, Uri file, String data) {
        return saveFile(context, file, data.getBytes(StandardCharsets.UTF_8));
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
            result = new String(content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
