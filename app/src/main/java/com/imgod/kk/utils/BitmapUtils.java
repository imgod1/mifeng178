package com.imgod.kk.utils;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * BitmapUtils.java是液总汇的类。
 *
 * @author imgod1
 * @version 2.0.0 2018/7/9 9:55
 * @update imgod1 2018/7/9 9:55
 * @updateDes
 * @include {@link }
 * @used {@link }
 */
public class BitmapUtils {
    public String Bitmap2StrByBase64(Bitmap bit, int quality) {
        if (quality <= 0 || quality > 100) {
            quality = 40;//默认40
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, quality, bos);
        byte[] bytes = bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}
