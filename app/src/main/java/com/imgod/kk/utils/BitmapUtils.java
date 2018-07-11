package com.imgod.kk.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class BitmapUtils {
    private static final String TAG = "BitmapUtils";

    public static String bitmapToBase64WithTitle(Bitmap bit, int quality) {
        return "data:image/jpg;base64," + bitmapToBase64WithCompressByPixelAndQuality(bit, quality);//1m以内
    }

    public static String bitmapToBase64WithCompressByPixelAndQuality(Bitmap bit, int quality) {
        return bitmapToBase64(compressScale(bit), quality, 1024 * 1000);//1m以内
    }


    public static String bitmapToBase64(Bitmap bit, int quality) {
        return bitmapToBase64(bit, quality, 1024 * 1000);//1m以内
    }

    public static String bitmapToBase64(Bitmap bit, int quality, int maxByteLength) {
        byte[] bytes = bitmap2ByteArrayByBase64(bit, quality, maxByteLength);
        LogUtils.e(TAG, "Bitmap2StrByBase64: " + bytes.length);
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
        if (bytes.length > maxByteLength && quality > 10) {
            quality -= 10;
            bytes = bitmap2ByteArrayByBase64(bit, quality, maxByteLength);
        }
        return bytes;
    }


    //base64转成bitmap
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


    //压缩图片分辨率
    public static void compressBitmapToFile(Bitmap bmp, File file, int ratio) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressScale(bmp).compress(Bitmap.CompressFormat.JPEG, 100, baos);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图片按比例大小压缩方法
     *
     * @param image （根据Bitmap图片压缩）
     * @return
     */
    public static Bitmap compressScale(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        // 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (baos.toByteArray().length / 1024 > 1024) {
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        Log.i(TAG, w + "---------------" + h);
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        // float hh = 800f;// 这里设置高度为800f
        // float ww = 480f;// 这里设置宽度为480f
        float hh = 800f;
        float ww = 480f;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) { // 如果高度高的话根据高度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be; // 设置缩放比例
        // newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return bitmap;
    }
}
