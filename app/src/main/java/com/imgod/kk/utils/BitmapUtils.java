package com.imgod.kk.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";

    public static String bitmapToBase64(Bitmap bit, int quality) {
        return bitmapToBase64(bit, quality, 1024 * 1000);//1m以内
    }

    public static String bitmapToBase64(Bitmap bit, int quality, int maxByteLength) {
        byte[] bytes = bitmap2ByteArrayByBase64(bit, quality, maxByteLength);
        LogUtils.e(TAG, "Bitmap2StrByBase64: " + bytes.length);
//        "data:image/jpg;base64," +
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] bitmap2ByteArrayByBase64(Bitmap bit, int quality, int maxByteLength) {
        if (quality > 100) {
            quality = 40;//默认一般40
        }
        if (quality <= 0) {
            quality = 10;//默认最低10
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, quality, bos);
        byte[] bytes = bos.toByteArray();
//        if (bytes.length > maxByteLength && quality > 10) {
//            quality -= 10;
//            bytes = bitmap2ByteArrayByBase64(bit, quality, maxByteLength);
//        }
        return bytes;
    }


    //base64转成bitmap
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
