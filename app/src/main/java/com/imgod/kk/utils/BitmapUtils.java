package com.imgod.kk.utils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";

    public static String Bitmap2StrByBase64(Bitmap bit, int quality) {
        return Bitmap2StrByBase64(bit, quality, 1024 * 1000);//1m以内
    }

    public static String Bitmap2StrByBase64(Bitmap bit, int quality, int maxByteLength) {
        byte[] bytes = Bitmap2ByteArrayByBase64(bit, quality, maxByteLength);
        LogUtils.e(TAG, "Bitmap2StrByBase64: " + bytes.length);
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] Bitmap2ByteArrayByBase64(Bitmap bit, int quality, int maxByteLength) {
        if (quality > 100) {
            quality = 40;//默认一般40
        }
        if (quality <= 0) {
            quality = 10;//默认最低10
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, quality, bos);
        byte[] bytes = bos.toByteArray();
        if (bytes.length > maxByteLength && quality > 10) {
            quality -= 10;
            bytes = Bitmap2ByteArrayByBase64(bit, quality, maxByteLength);
        }
        return bytes;
    }
}
