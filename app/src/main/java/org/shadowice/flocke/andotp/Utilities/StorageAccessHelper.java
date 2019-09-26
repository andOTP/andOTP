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
