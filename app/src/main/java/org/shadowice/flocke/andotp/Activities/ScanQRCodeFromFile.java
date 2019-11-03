package org.shadowice.flocke.andotp.Activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.StorageAccessHelper;
import org.shadowice.flocke.andotp.Utilities.Tools;
import java.io.IOException;

public class ScanQRCodeFromFile {
    public static String scanQRImage(Context context, Uri uri) {
        //Check if external storage is accessible
        if (!Tools.isExternalStorageReadable()) {
            Toast.makeText(context, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
            return null;
        }
        //Get image in bytes
        byte[] imageInBytes;
        try {
            imageInBytes = StorageAccessHelper.loadFile(context,uri);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,R.string.toast_file_load_error,Toast.LENGTH_LONG).show();
            return null;
        }

        Bitmap bMap = BitmapFactory.decodeByteArray(imageInBytes,0,imageInBytes.length);
        String contents = null;
        int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];

        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        //Try finding QR code
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context,R.string.toast_qr_error,Toast.LENGTH_LONG).show();
        } catch (ChecksumException e) {
            e.printStackTrace();
            Toast.makeText(context,R.string.toast_qr_checksum_exception,Toast.LENGTH_LONG).show();
        } catch (FormatException e) {
            e.printStackTrace();
            Toast.makeText(context,R.string.toast_qr_format_error,Toast.LENGTH_LONG).show();
        }
        //Return QR code (if found)
        return contents;
    }
}
