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

